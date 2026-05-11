package org.example.sourceofvoice.services;

import io.minio.Http;
import org.example.sourceofvoice.DTO.responses.audio.AudioFileResponse;
import org.example.sourceofvoice.DTO.responses.audio.AudioSubmissionResponse;
import org.example.sourceofvoice.DTO.responses.audio.ReviewerAudioSubmissionDetailsResponse;
import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.example.sourceofvoice.entities.text.AudioText;
import org.example.sourceofvoice.helper.PaymentClient;
import org.example.sourceofvoice.repositories.AudioSubmissionRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ReviewerAudioSubmissionService {

    private final AudioSubmissionRepository audioSubmissionRepository;
    private final AudioTextRepository audioTextRepository;
    private final AudioStorageService audioStorageService;
    private final AudioTextClosingService audioTextClosingService;
    private final String publicBaseUrl;
    private final PaymentClient paymentClient;

    public ReviewerAudioSubmissionService(PaymentClient paymentClient, AudioSubmissionRepository audioSubmissionRepository, @Value("${app.public-base-url}") String publicBaseUrl, AudioTextRepository audioTextRepository, AudioStorageService audioStorageService, AudioTextClosingService audioTextClosingService) {
        this.audioSubmissionRepository = audioSubmissionRepository;
        this.audioTextRepository = audioTextRepository;
        this.audioStorageService = audioStorageService;
        this.audioTextClosingService = audioTextClosingService;
        this.publicBaseUrl = publicBaseUrl;
        this.paymentClient = paymentClient;
    }

    public Mono<SliceResponse<AudioSubmissionResponse>> getAvailableSubmissions(int page, int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.ASC, "submittedAt")
        );

        return audioSubmissionRepository.findByStatusAndAssignedReviewerIdIsNull(
                AudioSubmissionStatus.NEEDS_REVIEW,
                pageable
        )
                .map(this::toResponse)
                .collectList()
                .map(items -> SliceResponse.of(items, safePage, safeSize));

    }

    public Mono<SliceResponse<AudioSubmissionResponse>> getMyAssignedSubmissions(
            Long reviewerId,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.ASC, "assignedAt")
        );

        return audioSubmissionRepository
                .findByAssignedReviewerIdAndStatus(
                        reviewerId,
                        AudioSubmissionStatus.IN_REVIEW,
                        pageable
                )
                .map(this::toResponse)
                .collectList()
                .map(items -> SliceResponse.of(items, safePage, safeSize));
    }

    public Mono<ReviewerAudioSubmissionDetailsResponse> getSubmissionDetails(
            Long submissionId,
            Long reviewerId
    ) {
        return audioSubmissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission not available"
                )))
                .flatMap(submission -> {
                    validateReviewerCanViewSubmission(submission, reviewerId);

                    return audioTextRepository.findById(submission.getAudioTextId())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Requested resource not allowed"
                            )))
                            .map(text -> toDetailsResponse(submission, text));
                });
    }

    public Mono<AudioSubmissionResponse> claimSubmission(Long submissionId, Long reviewerId){
        return audioSubmissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission not available"
                )))
                .flatMap(submission -> {
                    if(submission.getStatus() != AudioSubmissionStatus.NEEDS_REVIEW){
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Submission not available"
                        ));
                    }

                    if (submission.getAssignedReviewerId() != null) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Operation not allowed"
                        ));
                    }

                    submission.setStatus(AudioSubmissionStatus.IN_REVIEW);
                    submission.setAssignedReviewerId(reviewerId);
                    submission.setAssignedAt(LocalDateTime.now());

                    return audioSubmissionRepository.save(submission);
                })
                .map(this::toResponse);
    }

    public Mono<AudioSubmissionResponse> approveSubmission(Long submissionId, Long reviewerId){
        return audioSubmissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission not available"
                )))
                .flatMap(submission -> {
                    validateReviewerOwnsSubmission(submission,reviewerId);


                    submission.setStatus(AudioSubmissionStatus.APPROVED_FOR_PAYMENT);
                    submission.setReviewedBy(reviewerId);
                    submission.setReviewedAt(LocalDateTime.now());

                    return audioSubmissionRepository.save(submission)
                            .flatMap(saved ->
                                    paymentClient.rewardAudio(
                                                    saved.getUserId(),
                                                    saved.getId(),
                                                    saved.getPayoutAmount()
                                            )
                                            .then(audioTextClosingService.closeTextIfLimitReached(saved.getAudioTextId()))
                                            .thenReturn(saved)
                            );

                }).map(this::toResponse);
    }

    public Mono<AudioSubmissionResponse> rejectSubmission(Long submissionId, Long reviewerId){
        return audioSubmissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission not available"
                )))
                .flatMap(submission -> {
                    validateReviewerOwnsSubmission(submission,reviewerId);

                    submission.setStatus(AudioSubmissionStatus.REJECTED_FOR_PAYMENT);
                    submission.setReviewedBy(reviewerId);
                    submission.setReviewedAt(LocalDateTime.now());

                    return audioSubmissionRepository.save(submission);
                })
                .map(this::toResponse);
    }

    public Mono<AudioFileResponse> getSubmissionAudioFile(
            Long submissionId,
            Long reviewerId
    ) {
        return audioSubmissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submission not available"
                )))
                .flatMap(submission -> {
                    validateReviewerCanViewSubmission(submission, reviewerId);

                    return audioStorageService.getAudioBytes(
                                    submission.getBucketName(),
                                    submission.getObjectKey()
                            )
                            .map(bytes -> new AudioFileResponse(
                                    bytes,
                                    resolveContentType(submission.getContentType())
                            ));
                });
    }
    private String resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
    private void validateReviewerCanViewSubmission(
            AudioSubmission submission,
            Long reviewerId
    ) {
        if (submission.getAssignedReviewerId() == null) {
            return;
        }

        if (!submission.getAssignedReviewerId().equals(reviewerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Submission not available"
            );
        }
    }

    private void validateReviewerOwnsSubmission(
            AudioSubmission submission,
            Long reviewerId
    ) {
        if (submission.getStatus() != AudioSubmissionStatus.IN_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflict"
            );
        }

        if (submission.getAssignedReviewerId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflict"
            );
        }

        if (!submission.getAssignedReviewerId().equals(reviewerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden"
            );
        }
    }

    private AudioSubmissionResponse toResponse(AudioSubmission submission) {
        AudioSubmissionResponse response = new AudioSubmissionResponse();

        response.setId(submission.getId());
        response.setAudioTextId(submission.getAudioTextId());
        response.setStatus(submission.getStatus());
        response.setCorrectnessScore(submission.getCorrectnessScore());
        response.setPayoutAmount(submission.getPayoutAmount());

        return response;
    }

    private ReviewerAudioSubmissionDetailsResponse toDetailsResponse(
            AudioSubmission submission,
            AudioText text
    ) {
        ReviewerAudioSubmissionDetailsResponse response =
                new ReviewerAudioSubmissionDetailsResponse();

        response.setId(submission.getId());
        response.setUserId(submission.getUserId());
        response.setAudioTextId(submission.getAudioTextId());
        response.setStatus(submission.getStatus());

        response.setSourceTitle(text.getSourceTitle());

        response.setOriginalText(text.getContent());
        response.setTranscriptText(submission.getTranscriptText());

        response.setAudioUrl(
                publicBaseUrl + "/sofv/reviewer/audio/"
                        + submission.getId()
                        + "/file"
        );

        response.setCorrectnessScore(submission.getCorrectnessScore());
        response.setPayoutAmount(submission.getPayoutAmount());

        response.setAssignedReviewerId(submission.getAssignedReviewerId());
        response.setAssignedAt(submission.getAssignedAt());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setTranscribedAt(submission.getTranscribedAt());

        return response;
    }
}
