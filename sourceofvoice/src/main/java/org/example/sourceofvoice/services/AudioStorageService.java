package org.example.sourceofvoice.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.sourceofvoice.helper.StoredAudioFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class AudioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public AudioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public Mono<StoredAudioFile> storeAudio(
            Path tempFile,
            String safeOriginalFilename,
            Long userId,
            Long audioTextId
    ) {
        return Mono.fromCallable(() -> {
            String objectKey = buildObjectKey(userId, audioTextId, safeOriginalFilename);
            String contentType = resolveContentType(safeOriginalFilename);
            long fileSize = Files.size(tempFile);

            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectKey)
                                .stream(inputStream, fileSize, -1L)
                                .contentType(contentType)
                                .build()
                );
            }

            return new StoredAudioFile(
                    bucketName,
                    objectKey,
                    safeOriginalFilename,
                    contentType,
                    fileSize
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<byte[]> getAudioBytes(String bucketName, String objectKey) {
        return Mono.fromCallable(() -> {
            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            )) {
                return inputStream.readAllBytes();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String buildObjectKey(Long userId, Long audioTextId, String safeOriginalFilename) {
        String extension = extractExtension(safeOriginalFilename);

        return "users/" + userId
                + "/texts/" + audioTextId
                + "/" + UUID.randomUUID()
                + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return ".bin";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return ".bin";
        }

        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String resolveContentType(String safeOriginalFilename) {
        String lower = safeOriginalFilename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".wav")) {
            return "audio/wav";
        }
        if (lower.endsWith(".mp3")) {
            return "audio/mpeg";
        }
        if (lower.endsWith(".aac")) {
            return "audio/aac";
        }
        if (lower.endsWith(".ogg")) {
            return "audio/ogg";
        }
        if (lower.endsWith(".mpeg")) {
            return "audio/mpeg";
        }
        if (lower.endsWith(".amr")) {
            return "audio/amr";
        }
        if (lower.endsWith(".m4a")) {
            return "audio/mp4";
        }
        if (lower.endsWith(".mp4")) {
            return "audio/mp4";
        }
        if (lower.endsWith(".flac")) {
            return "audio/flac";
        }

        return "application/octet-stream";
    }
}
