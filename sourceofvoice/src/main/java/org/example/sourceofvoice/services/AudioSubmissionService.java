package org.example.sourceofvoice.services;

import org.example.sourceofvoice.DTO.responses.audio.AudioSubmissionResponse;
import org.example.sourceofvoice.DTO.responses.text.SliceResponse;
import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.example.sourceofvoice.entities.text.AudioText;
import org.example.sourceofvoice.entities.text.AudioTextStatus;
import org.example.sourceofvoice.repositories.AudioSubmissionRepository;
import org.example.sourceofvoice.repositories.AudioTextRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AudioSubmissionService {

    private static final long MAX_AUDIO_SIZE_BYTES = 20L * 1024L * 1024L;

    private static final Pattern SAFE_FILENAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{1,100}$");

    private final AudioStorageService audioStorageService;
    private final AudioSubmissionRepository audioSubmissionRepository;
    private final AudioTextRepository audioTextRepository;

    @Value("${spring.webflux.multipart.file-storage-directory:/tmp/sourceofvoice-upload}")
    private String multipartTempDirectory;

    public AudioSubmissionService(AudioStorageService audioStorageService,
                                  AudioSubmissionRepository audioSubmissionRepository,
                                  AudioTextRepository audioTextRepository) {
        this.audioStorageService = audioStorageService;
        this.audioSubmissionRepository = audioSubmissionRepository;
        this.audioTextRepository = audioTextRepository;
    }

    public Mono<AudioSubmissionResponse> submitAudio(Long audioTextId, Long userId, FilePart file) {
        return audioTextRepository.findByIdAndStatus(audioTextId, AudioTextStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Requested resource not found"
                )))
                .flatMap(text -> saveAndProcessTempFile(text, userId, file))
                .map(this::toResponse);
    }

    private Mono<AudioSubmission> saveAndProcessTempFile(AudioText text, Long userId, FilePart file) {
        String safeOriginalFilename = validateAndNormalizeFilename(file);

        return createTempFile()
                .flatMap(tempFile ->
                        file.transferTo(tempFile)
                                .then(validateTempFile(tempFile))
                                .flatMap(validatedPath -> createSubmission(
                                        text,
                                        userId,
                                        validatedPath,
                                        safeOriginalFilename
                                ))
                                .doFinally(signal -> deleteTempFileQuietly(tempFile))
                );
    }

    private Mono<Path> createTempFile() {
        return Mono.fromCallable(() -> {
                    Path uploadDir = Paths.get(multipartTempDirectory).toAbsolutePath().normalize();
                    Files.createDirectories(uploadDir);
                    return Files.createTempFile(uploadDir, "upload-", ".tmp");
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Path> validateTempFile(Path tempFile) {
        return Mono.fromCallable(() -> {
                    long size = Files.size(tempFile);

                    if (size == 0) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Audio file is empty"
                        );
                    }

                    if (size > MAX_AUDIO_SIZE_BYTES) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Audio file is too large"
                        );
                    }

                    return tempFile;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String validateAndNormalizeFilename(FilePart file) {
        String rawFilename = file.filename();

        if (rawFilename == null || rawFilename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Audio filename is required"
            );
        }

        String normalizedSeparators = rawFilename.replace("\\", "/");
        String baseName = Paths.get(normalizedSeparators).getFileName().toString();

        if (baseName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Audio filename is required"
            );
        }

        if (baseName.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported audio file name"
            );
        }

        if (!SAFE_FILENAME_PATTERN.matcher(baseName).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported audio file name"
            );
        }

        String lowerFilename = baseName.toLowerCase();

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

        return baseName;
    }

    public Mono<SliceResponse<AudioSubmissionResponse>> getUserSubmissions(Long userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "submittedAt")
        );

        return audioSubmissionRepository.findByUserId(userId, pageable)
                .map(this::toResponse)
                .collectList()
                .map(items -> SliceResponse.of(items, safePage, safeSize));
    }

    private Mono<AudioSubmission> createSubmission(AudioText text,
                                                   Long userId,
                                                   Path tempFile,
                                                   String safeOriginalFilename) {
        return audioStorageService.storeAudio(tempFile, safeOriginalFilename, userId, text.getId())
                .flatMap(storedFile -> {
                    AudioSubmission submission = AudioSubmission.builder()
                            .userId(userId)
                            .audioTextId(text.getId())
                            .status(AudioSubmissionStatus.SUBMITTED)
                            .bucketName(storedFile.getBucketName())
                            .objectKey(storedFile.getObjectKey())
                            .originalFileName(storedFile.getOriginalFileName())
                            .contentType(storedFile.getContentType())
                            .fileSizeBytes(storedFile.getFileSizeBytes())
                            .payoutAmount(text.getBasePrice())
                            .submittedAt(LocalDateTime.now())
                            .build();

                    return audioSubmissionRepository.save(submission);
                });
    }

    private void deleteTempFileQuietly(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (Exception ignored) {
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
