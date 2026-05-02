package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.responses.audio.AudioSubmissionResponse;
import org.example.sourceofvoice.DTO.responses.audio.ReviewerAudioSubmissionDetailsResponse;
import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.services.ReviewerAudioSubmissionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv/reviewer/audio")
public class ReviewerAudioSubmissionController {

    private final ReviewerAudioSubmissionService service;

    public ReviewerAudioSubmissionController(ReviewerAudioSubmissionService service) {
        this.service = service;
    }

    @GetMapping("/available")
    public Mono<SliceResponse<AudioSubmissionResponse>> getAvailableSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.getAvailableSubmissions(page, size);
    }

    @GetMapping("/my")
    public Mono<SliceResponse<AudioSubmissionResponse>> getMyAssignedSubmissions(
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.getMyAssignedSubmissions(reviewerId, page, size);
    }

    @GetMapping("/{id}")
    public Mono<ReviewerAudioSubmissionDetailsResponse> getSubmissionDetails(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        return service.getSubmissionDetails(id, reviewerId);
    }
    @GetMapping("/{id}/file")
    public Mono<ResponseEntity<byte[]>> getSubmissionAudioFile(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        return service.getSubmissionAudioFile(id, reviewerId)
                .map(file -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(file.getContentType()))
                        .body(file.getBytes()));
    }

    @PatchMapping("/{id}/claim")
    public Mono<AudioSubmissionResponse> claimSubmission(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        return service.claimSubmission(id, reviewerId);
    }

    @PatchMapping("/{id}/approve")
    public Mono<AudioSubmissionResponse> approveSubmission(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        return service.approveSubmission(id, reviewerId);
    }

    @PatchMapping("/{id}/reject")
    public Mono<AudioSubmissionResponse> rejectSubmission(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        return service.rejectSubmission(id, reviewerId);
    }
}