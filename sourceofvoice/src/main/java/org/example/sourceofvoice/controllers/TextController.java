package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.responses.SliceResponse;
import org.example.sourceofvoice.DTO.responses.UserAudioTextDetailsResponse;
import org.example.sourceofvoice.DTO.responses.UserAudioTextListItemResponse;
import org.example.sourceofvoice.services.TextService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv")
public class TextController {

    private final TextService textService;

    public TextController(TextService textService) {
        this.textService = textService;
    }

    @GetMapping("/texts")
    public Mono<SliceResponse<UserAudioTextListItemResponse>> getActiveTexts(
            @RequestParam(defaultValue = "en") String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return textService.getActiveTexts(languageCode, page, size);
    }

    @GetMapping("/texts/{id}")
    public Mono<UserAudioTextDetailsResponse> getActiveTextById(
            @PathVariable Long id
    ) {
        return textService.getActiveTextDetails(id);
    }
}
