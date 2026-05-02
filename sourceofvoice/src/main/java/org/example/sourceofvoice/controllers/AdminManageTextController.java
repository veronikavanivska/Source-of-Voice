package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.responses.text.*;
import org.example.sourceofvoice.entities.text.AudioTextStatus;
import org.example.sourceofvoice.services.AdminManageTextService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv/admin")
public class AdminManageTextController {

    private final AdminManageTextService adminManageTextService;

    public AdminManageTextController(AdminManageTextService adminManageTextService) {
        this.adminManageTextService = adminManageTextService;
    }

    @GetMapping("/textBatches")
    public Mono<SliceResponse<AdminAudioTextBatchListItemResponse>> getBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return  adminManageTextService.getBatches(page,size);
    }

    @GetMapping("/textBatches/{id}")
    public Mono<AdminAudioTextBatchDetailsResponse> getBatchById(
            @PathVariable Long id
    ){
        return adminManageTextService.getBatchById(id);
    }

    @GetMapping("/text")
    public Mono<SliceResponse<AdminAudioTextListItemResponse>> getTexts(
            @RequestParam(required = false) AudioTextStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return adminManageTextService.getTexts(status,page,size);
    }

    @GetMapping("/text/{id}")
    public Mono<AdminAudioTextDetailsResponse> getTextById(
            @PathVariable Long id
    ){
        return adminManageTextService.getTextById(id);
    }

    @PatchMapping("/texts/{id}/activate")
    public Mono<AudioTextStatusResponse> activateText(
            @PathVariable Long id
    ) {
        return adminManageTextService.activateText(id);
    }

    @PatchMapping("/texts/{id}/disable")
    public Mono<AudioTextStatusResponse> disableText(
            @PathVariable Long id
    ) {
        return adminManageTextService.disableText(id);
    }

    @PatchMapping("/texts/{id}/archive")
    public Mono<AudioTextStatusResponse> archiveText(
            @PathVariable Long id
    ) {
        return adminManageTextService.archiveText(id);
    }

}
