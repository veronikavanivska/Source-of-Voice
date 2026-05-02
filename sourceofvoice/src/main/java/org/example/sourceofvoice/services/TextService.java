package org.example.sourceofvoice.services;

import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.DTO.responses.text.UserAudioTextDetailsResponse;
import org.example.sourceofvoice.DTO.responses.text.UserAudioTextListItemResponse;
import org.example.sourceofvoice.entities.text.AudioText;
import org.example.sourceofvoice.entities.text.AudioTextStatus;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class TextService {


    private final AudioTextRepository audioTextRepository;

    public TextService(AudioTextRepository audioTextRepository) {
        this.audioTextRepository = audioTextRepository;
    }

    public Mono<SliceResponse<UserAudioTextListItemResponse>>  getActiveTexts(String languageCode, int page, int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return audioTextRepository.findByLanguageCodeAndStatus(languageCode, AudioTextStatus.ACTIVE, pageable)
                .map(this::toUserListItem)
                .collectList()
                .map(items -> SliceResponse.of(items, safePage, safeSize));

    }

    public Mono<UserAudioTextDetailsResponse> getActiveTextDetails(Long id){
        return audioTextRepository.findByIdAndStatus(id, AudioTextStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Audion text not found")))
                .map(this::toUserDetails);
    }

    private UserAudioTextListItemResponse toUserListItem(AudioText text) {
        UserAudioTextListItemResponse response = new UserAudioTextListItemResponse();

        response.setId(text.getId());
        response.setLanguageCode(text.getLanguageCode());
        response.setSourceTitle(text.getSourceTitle());
        response.setWordCount(text.getWordCount());
        response.setDifficultyScore(text.getDifficultyScore());
        response.setEstimatedReadingSeconds(text.getEstimatedReadingSeconds());
        response.setBasePrice(text.getBasePrice());

        return response;
    }

    private UserAudioTextDetailsResponse toUserDetails(AudioText text) {
        UserAudioTextDetailsResponse response = new UserAudioTextDetailsResponse();

        response.setId(text.getId());
        response.setLanguageCode(text.getLanguageCode());
        response.setSourceTitle(text.getSourceTitle());
        response.setContent(text.getContent());
        response.setWordCount(text.getWordCount());
        response.setDifficultyScore(text.getDifficultyScore());
        response.setEstimatedReadingSeconds(text.getEstimatedReadingSeconds());
        response.setBasePrice(text.getBasePrice());

        return response;
    }
}

