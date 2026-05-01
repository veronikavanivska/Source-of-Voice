package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.requests.GenerateWikipediaTextsRequest;
import org.example.sourceofvoice.DTO.responses.GenerateWikipediaTextsResponse;
import org.example.sourceofvoice.services.AdminTextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv/admin/texts")
public class AdminTextController {

    private final AdminTextService adminTextService;

    public AdminTextController(AdminTextService adminTextService) {
        this.adminTextService = adminTextService;
    }

    @PostMapping("/generate")
    public Mono<GenerateWikipediaTextsResponse> generateFromWikipedia(
            @RequestBody GenerateWikipediaTextsRequest request
            ,@RequestHeader("X-User-Id") Long userId
    ) {
        return adminTextService.generateTextFromWikipedia(request, userId);
    }
}
