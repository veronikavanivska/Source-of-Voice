package org.example.sourceofvoice.controllers;

import org.example.sourceofvoice.DTO.responses.audio.AudioSubmissionResponse;
import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.services.AudioSubmissionService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sofv/audio")
public class AudioSubmissionController {

    private final AudioSubmissionService audioSubmissionService;

    public AudioSubmissionController(AudioSubmissionService audioSubmissionService) {
        this.audioSubmissionService = audioSubmissionService;
    }

    @PostMapping(
            value = "/{audioTextId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<AudioSubmissionResponse> submitAudio(
            @PathVariable Long audioTextId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("file") FilePart file
    ) {
        return audioSubmissionService.submitAudio(audioTextId, userId, file);
    }

    @GetMapping("/my")
    public Mono<SliceResponse<AudioSubmissionResponse>> getMySubmissions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return audioSubmissionService.getUserSubmissions(userId, page, size);
    }
}
