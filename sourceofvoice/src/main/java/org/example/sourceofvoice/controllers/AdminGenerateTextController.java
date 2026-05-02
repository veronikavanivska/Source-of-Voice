package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.requests.text.GenerateWikipediaTextsRequest;
import org.example.sourceofvoice.DTO.responses.text.GenerateWikipediaTextsResponse;
import org.example.sourceofvoice.services.AdminGenerateTextService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv/admin/texts")
public class AdminGenerateTextController {

    private final AdminGenerateTextService adminGenerateTextService;

    public AdminGenerateTextController(AdminGenerateTextService adminGenerateTextService) {
        this.adminGenerateTextService = adminGenerateTextService;
    }

    @PostMapping("/generate")
    public Mono<GenerateWikipediaTextsResponse> generateFromWikipedia(
            @RequestBody GenerateWikipediaTextsRequest request
            ,@RequestHeader("X-User-Id") Long userId
    ) {
        return adminGenerateTextService.generateTextFromWikipedia(request, userId);
    }
}
