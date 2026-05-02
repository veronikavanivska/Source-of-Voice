package org.example.sourceofvoice.services;

import org.example.sourceofvoice.DTO.responses.audio.AudioSubmissionResponse;
import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.example.sourceofvoice.entities.text.AudioText;
import org.example.sourceofvoice.entities.text.AudioTextStatus;
import org.example.sourceofvoice.repositories.AudioSubmissionRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class AudioSubmissionService {
    private static final long MAX_AUDIO_SIZE_BYTES = 20L * 1024L * 1024L;
    private final AudioStorageService audioStorageService;
    private final AudioSubmissionRepository audioSubmissionRepository;
    private final AudioTextRepository audioTextRepository;

    public AudioSubmissionService(AudioStorageService audioStorageService, AudioSubmissionRepository audioSubmissionRepository, AudioTextRepository audioTextRepository) {
        this.audioStorageService = audioStorageService;
        this.audioSubmissionRepository = audioSubmissionRepository;
        this.audioTextRepository = audioTextRepository;
    }

    public Mono<AudioSubmissionResponse> submitAudio(Long audioTextId, Long userId, FilePart file){
        return audioTextRepository.findByIdAndStatus(audioTextId, AudioTextStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active text for audio not found"
                )))
                .flatMap(text -> readFileBytes(file)
                        .flatMap(bytes -> createSubmission(text,userId,file,bytes)))
                .map(this::toResponse);
    }

    public Mono<SliceResponse<AudioSubmissionResponse>> getUserSubmissions(Long userId, int page, int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "submittedAt")
        );

        return audioSubmissionRepository.findByUserId(userId,pageable)
                .map(this::toResponse)
                .collectList()
                .map(items -> SliceResponse.of(items,safePage,safeSize));

    }

    private Mono<AudioSubmission> createSubmission(AudioText text, Long userId, FilePart file, byte[] bytes){
        validateAudio(file, bytes);

        return audioStorageService.storeAudio(file, bytes, userId, text.getId()).flatMap(storeFile -> {
            AudioSubmission submission = AudioSubmission.builder()
                    .userId(userId)
                    .audioTextId(text.getId())
                    .status(AudioSubmissionStatus.SUBMITTED)
                    .bucketName(storeFile.getBucketName())
                    .objectKey(storeFile.getObjectKey())
                    .originalFileName(storeFile.getOriginalFileName())
                    .contentType(storeFile.getContentType())
                    .fileSizeBytes(storeFile.getFileSizeBytes())
                    .payoutAmount(text.getBasePrice())
                    .submittedAt(LocalDateTime.now())
                    .build();

            return audioSubmissionRepository.save(submission);
        });
    }

    private Mono<byte[]> readFileBytes(FilePart file){
        return file.content()
                .reduce(new ByteArrayOutputStream(), (outputStream, dataBuffer) -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        outputStream.write(bytes);
                        return outputStream;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .map(ByteArrayOutputStream::toByteArray);
    }

    private void validateAudio(FilePart file, byte[] bytes) {
        if (bytes.length == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Audio file is empty"
            );
        }

        if (bytes.length > MAX_AUDIO_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Audio file is too large"
            );
        }

        String filename = file.filename();

        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Audio filename is required"
            );
        }

        String lowerFilename = filename.toLowerCase();

        boolean allowed = lowerFilename.endsWith(".wav")
                || lowerFilename.endsWith(".mp3")
                || lowerFilename.endsWith(".aac")
                || lowerFilename.endsWith(".ogg")
                || lowerFilename.endsWith(".mpeg")
                || lowerFilename.endsWith(".amr")
                || lowerFilename.endsWith(".m4a")
                || lowerFilename.endsWith(".mp4")
                || lowerFilename.endsWith(".flac");

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported audio file type"
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
}

