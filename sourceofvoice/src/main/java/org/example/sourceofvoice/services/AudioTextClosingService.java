package org.example.sourceofvoice.services;

import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.example.sourceofvoice.entities.text.AudioTextStatus;
import org.example.sourceofvoice.repositories.AudioSubmissionRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AudioTextClosingService {

    private final AudioSubmissionRepository audioSubmissionRepository;
    private final AudioTextRepository audioTextRepository;
    private final int maxApprovedSubmissions;

    public AudioTextClosingService(AudioSubmissionRepository audioSubmissionRepository, AudioTextRepository audioTextRepository,  @Value("${audio-text.max-approved-submissions:5}") int maxApprovedSubmissions) {
        this.audioSubmissionRepository = audioSubmissionRepository;
        this.audioTextRepository = audioTextRepository;
        this.maxApprovedSubmissions = maxApprovedSubmissions;
    }

    public Mono<Void> closeTextIfLimitReached(Long audioTextId) {
        List<AudioSubmissionStatus> payableStatuses = List.of(
                AudioSubmissionStatus.APPROVED_FOR_PAYMENT
        );

        return audioSubmissionRepository
                .countByAudioTextIdAndStatusIn(audioTextId, payableStatuses)
                .flatMap(approvedCount -> {
                    if (approvedCount < maxApprovedSubmissions) {
                        return Mono.empty();
                    }

                    return audioTextRepository.findById(audioTextId)
                            .flatMap(text -> {
                                if (text.getStatus() == AudioTextStatus.ARCHIVED) {
                                    return Mono.empty();
                                }

                                text.setStatus(AudioTextStatus.ARCHIVED);

                                return audioTextRepository.save(text).then();
                            });
                });
    }
}
