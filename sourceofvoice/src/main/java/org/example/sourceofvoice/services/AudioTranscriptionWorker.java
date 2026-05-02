package org.example.sourceofvoice.services;

import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.example.sourceofvoice.entities.text.AudioText;
import org.example.sourceofvoice.helper.PaymentClient;
import org.example.sourceofvoice.helper.SpeechmanticsClient;
import org.example.sourceofvoice.repositories.AudioSubmissionRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class AudioTranscriptionWorker {
    private final AudioSubmissionRepository audioSubmissionRepository;
    private final SpeechmanticsClient speechmanticsClient;
    private final AudioTextRepository audioTextRepository;
    private final TextSimilarityService textSimilarityService;
    private final AudioStorageService audioStorageService;
    private final AudioTextClosingService audioTextClosingService;
    private final PaymentClient paymentClient;

    public AudioTranscriptionWorker(AudioSubmissionRepository audioSubmissionRepository, SpeechmanticsClient speechmanticsClient, AudioTextRepository audioTextRepository, TextSimilarityService textSimilarityService, AudioStorageService audioStorageService, AudioTextClosingService audioTextClosingService, PaymentClient paymentClient) {
        this.audioSubmissionRepository = audioSubmissionRepository;
        this.speechmanticsClient = speechmanticsClient;
        this.audioTextRepository = audioTextRepository;
        this.textSimilarityService = textSimilarityService;
        this.audioStorageService = audioStorageService;
        this.audioTextClosingService = audioTextClosingService;
        this.paymentClient = paymentClient;
    }

    @Scheduled(fixedDelay = 10000)
    public void processSubmittedAudio() {
        audioSubmissionRepository.findByStatus(AudioSubmissionStatus.SUBMITTED)
                .take(5)
                .flatMap(this::sendToSpeechmatics)
                .subscribe();
    }


    @Scheduled(fixedDelay = 15000)
    public void checkTranscriptionJobs() {
        audioSubmissionRepository.findByStatus(AudioSubmissionStatus.TRANSCRIPTION_REQUESTED)
                .mergeWith(audioSubmissionRepository.findByStatus(AudioSubmissionStatus.TRANSCRIBING))
                .take(10)
                .flatMap(this::checkSpeechmaticsJob)
                .subscribe();
    }

    private Mono<AudioSubmission> sendToSpeechmatics(AudioSubmission submission) {
        return audioTextRepository.findById(submission.getAudioTextId())
                .flatMap(text ->
                        audioStorageService.getAudioBytes(
                                        submission.getBucketName(),
                                        submission.getObjectKey()
                                )
                                .flatMap(bytes ->
                                        speechmanticsClient.createTranscriptionJob(
                                                bytes,
                                                submission.getOriginalFileName(),
                                                text.getLanguageCode()
                                        )
                                )
                                .flatMap(job -> {
                                    submission.setSpeechmaticsJobId(job.getId());
                                    submission.setStatus(AudioSubmissionStatus.TRANSCRIPTION_REQUESTED);

                                    return audioSubmissionRepository.save(submission);
                                })
                )
                .onErrorResume(error -> {
                    submission.setStatus(AudioSubmissionStatus.TRANSCRIPTION_FAILED);
                    return audioSubmissionRepository.save(submission);
                });
    }

    private Mono<AudioSubmission> checkSpeechmaticsJob(AudioSubmission submission) {
        if (submission.getSpeechmaticsJobId() == null) {
            submission.setStatus(AudioSubmissionStatus.TRANSCRIPTION_FAILED);
            return audioSubmissionRepository.save(submission);
        }

        return speechmanticsClient.getJobDetails(submission.getSpeechmaticsJobId())
                .flatMap(details -> {
                    String jobStatus = null;

                    if (details.getJob() != null) {
                        jobStatus = details.getJob().getStatus();
                    }

                    if ("done".equalsIgnoreCase(jobStatus)) {
                        return fetchTranscriptAndScore(submission);
                    }

                    if ("rejected".equalsIgnoreCase(jobStatus)) {
                        submission.setStatus(AudioSubmissionStatus.TRANSCRIPTION_FAILED);
                        return audioSubmissionRepository.save(submission);
                    }

                    submission.setStatus(AudioSubmissionStatus.TRANSCRIBING);
                    return audioSubmissionRepository.save(submission);
                })
                .onErrorResume(error -> {
                    submission.setStatus(AudioSubmissionStatus.TRANSCRIPTION_FAILED);
                    return audioSubmissionRepository.save(submission);
                });
    }

    private Mono<AudioSubmission> fetchTranscriptAndScore(AudioSubmission submission) {
        return audioTextRepository.findById(submission.getAudioTextId())
                .flatMap(text ->
                        speechmanticsClient.getTranscriptText(submission.getSpeechmaticsJobId())
                                .flatMap(transcript -> updateSubmissionScore(submission, text, transcript))
                );
    }

    private Mono<AudioSubmission> updateSubmissionScore(
            AudioSubmission submission,
            AudioText text,
            String transcript
    ) {
        double score = textSimilarityService.calculateScore(
                text.getContent(),
                transcript
        );

        submission.setTranscriptText(transcript);
        submission.setCorrectnessScore(score);
        submission.setTranscribedAt(LocalDateTime.now());
        submission.setStatus(resolveStatus(score));

        return audioSubmissionRepository.save(submission)
                .flatMap(saved -> {
                    if (saved.getStatus() == AudioSubmissionStatus.APPROVED_FOR_PAYMENT) {
                        return paymentClient.rewardAudio(
                                        saved.getUserId(),
                                        saved.getId(),
                                        saved.getPayoutAmount()
                                )
                                .then(audioTextClosingService.closeTextIfLimitReached(saved.getAudioTextId()))
                                .thenReturn(saved);
                    }

                     return Mono.just(saved);
                });
    }

    private AudioSubmissionStatus resolveStatus(double score) {
        if (score < 70.0) {
            return AudioSubmissionStatus.AUTO_REJECTED;
        }

        if (score <= 90.0) {
            return AudioSubmissionStatus.NEEDS_REVIEW;
        }

        return AudioSubmissionStatus.APPROVED_FOR_PAYMENT;
    }
}
