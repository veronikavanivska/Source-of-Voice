package org.example.sourceofvoice.services;

import org.example.sourceofvoice.DTO.responses.*;
import org.example.sourceofvoice.entities.AudioText;
import org.example.sourceofvoice.entities.AudioTextBatch;
import org.example.sourceofvoice.entities.AudioTextStatus;
import org.example.sourceofvoice.repositories.AudioTextBatchRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
public class AdminManageTextService {

    private final AudioTextBatchRepository audioTextBatchRepository;
    private final AudioTextRepository audioTextRepository;

    public AdminManageTextService(AudioTextBatchRepository audioTextBatchRepository, AudioTextRepository audioTextRepository) {
        this.audioTextBatchRepository = audioTextBatchRepository;
        this.audioTextRepository = audioTextRepository;
    }

    public Mono<SliceResponse<AdminAudioTextBatchListItemResponse>> getBatches(int page , int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "startedAt")
        );

        return audioTextBatchRepository.findAllBy(pageable)
                .map(this::toBatchListItem)
                .collectList()
                .map(items -> SliceResponse.of(items,safePage,safeSize));

    }

    public Mono<AdminAudioTextBatchDetailsResponse> getBatchById(Long id){
        return audioTextBatchRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"Batch not found"
                        )))
                .map(this::toBatchDetails);
    }

    public Mono<SliceResponse<AdminAudioTextListItemResponse>> getTexts(AudioTextStatus status, int page, int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        if (status == null) {

            return audioTextRepository.findAllBy(pageable)
                    .map(this::toAdminTextListItem)
                    .collectList()
                    .map(items -> SliceResponse.of(items, safePage, safeSize));
        }

        return audioTextRepository.findByStatus(status, pageable)
                .map(this::toAdminTextListItem)
                .collectList()
                .map(items -> SliceResponse.of(items, safePage, safeSize));
    }

    public Mono<AdminAudioTextDetailsResponse> getTextById(Long id){
        return audioTextRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Audio text not found"
                )))
                .map(this::toAdminTextDetails);
    }

    public Mono<AudioTextStatusResponse> activateText(Long id) {
        return audioTextRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Audio text not found"
                )))
                .flatMap(text -> {
                    text.setStatus(AudioTextStatus.ACTIVE);
                    text.setActivatedAt(LocalDateTime.now());
                    text.setDisabledAt(null);

                    return audioTextRepository.save(text);
                })
                .map(this::toStatusResponse);
    }

    public Mono<AudioTextStatusResponse> disableText(Long id) {
        return audioTextRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Audio text not found"
                )))
                .flatMap(text -> {
                    text.setStatus(AudioTextStatus.DISABLED);
                    text.setDisabledAt(LocalDateTime.now());

                    return audioTextRepository.save(text);
                })
                .map(this::toStatusResponse);
    }

    public Mono<AudioTextStatusResponse> archiveText(Long id) {
        return audioTextRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Audio text not found"
                )))
                .flatMap(text -> {
                    text.setStatus(AudioTextStatus.ARCHIVED);

                    return audioTextRepository.save(text);
                })
                .map(this::toStatusResponse);
    }

    private AdminAudioTextBatchListItemResponse toBatchListItem(AudioTextBatch batch) {
        AdminAudioTextBatchListItemResponse response = new AdminAudioTextBatchListItemResponse();

        response.setId(batch.getId());
        response.setCreatedByAdminId(batch.getCreatedByAdminId());
        response.setStatus(batch.getStatus());
        response.setLanguageCode(batch.getLanguageCode());
        response.setRequestedCount(batch.getRequestedCount());
        response.setSavedCount(batch.getSavedCount());
        response.setSkippedCount(batch.getSkippedCount());
        response.setMinWords(batch.getMinWords());
        response.setMaxWords(batch.getMaxWords());
        response.setStartedAt(batch.getStartedAt());
        response.setFinishedAt(batch.getFinishedAt());

        return response;
    }

    private AdminAudioTextBatchDetailsResponse toBatchDetails(AudioTextBatch batch) {
        AdminAudioTextBatchDetailsResponse response = new AdminAudioTextBatchDetailsResponse();

        response.setId(batch.getId());
        response.setCreatedByAdminId(batch.getCreatedByAdminId());
        response.setStatus(batch.getStatus());
        response.setLanguageCode(batch.getLanguageCode());
        response.setRequestedCount(batch.getRequestedCount());
        response.setSavedCount(batch.getSavedCount());
        response.setSkippedCount(batch.getSkippedCount());
        response.setMinWords(batch.getMinWords());
        response.setMaxWords(batch.getMaxWords());
        response.setErrorMessage(batch.getErrorMessage());
        response.setStartedAt(batch.getStartedAt());
        response.setFinishedAt(batch.getFinishedAt());

        return response;
    }

    private AdminAudioTextListItemResponse toAdminTextListItem(AudioText text) {
        AdminAudioTextListItemResponse response = new AdminAudioTextListItemResponse();

        response.setId(text.getId());
        response.setBatchId(text.getBatchId());
        response.setStatus(text.getStatus());
        response.setLanguageCode(text.getLanguageCode());
        response.setSourcePageId(text.getSourcePageId());
        response.setSourceTitle(text.getSourceTitle());
        response.setSourceUrl(text.getSourceUrl());
        response.setWordCount(text.getWordCount());
        response.setCharacterCount(text.getCharacterCount());
        response.setDifficultyScore(text.getDifficultyScore());
        response.setEstimatedReadingSeconds(text.getEstimatedReadingSeconds());
        response.setBasePrice(text.getBasePrice());
        response.setCreatedAt(text.getCreatedAt());
        response.setActivatedAt(text.getActivatedAt());
        response.setDisabledAt(text.getDisabledAt());

        return response;
    }

    private AdminAudioTextDetailsResponse toAdminTextDetails(AudioText text) {
        AdminAudioTextDetailsResponse response = new AdminAudioTextDetailsResponse();

        response.setId(text.getId());
        response.setBatchId(text.getBatchId());
        response.setStatus(text.getStatus());
        response.setLanguageCode(text.getLanguageCode());
        response.setSourcePageId(text.getSourcePageId());
        response.setSourceTitle(text.getSourceTitle());
        response.setSourceUrl(text.getSourceUrl());
        response.setContent(text.getContent());
        response.setWordCount(text.getWordCount());
        response.setCharacterCount(text.getCharacterCount());
        response.setDifficultyScore(text.getDifficultyScore());
        response.setEstimatedReadingSeconds(text.getEstimatedReadingSeconds());
        response.setBasePrice(text.getBasePrice());
        response.setCreatedAt(text.getCreatedAt());
        response.setActivatedAt(text.getActivatedAt());
        response.setDisabledAt(text.getDisabledAt());

        return response;
    }

    private AudioTextStatusResponse toStatusResponse(AudioText text) {
        AudioTextStatusResponse response = new AudioTextStatusResponse();

        response.setId(text.getId());
        response.setStatus(text.getStatus());
        response.setActivatedAt(text.getActivatedAt());
        response.setDisabledAt(text.getDisabledAt());

        return response;
    }
}

