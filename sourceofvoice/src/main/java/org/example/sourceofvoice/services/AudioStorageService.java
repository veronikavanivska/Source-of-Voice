package org.example.sourceofvoice.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.sourceofvoice.helper.StoredAudioFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
            FilePart file,
            byte[] bytes,
            Long userId,
            Long audioTextId
    ) {
        return Mono.fromCallable(() -> {
            String objectKey = buildObjectKey(userId, audioTextId, file.filename());
            String contentType = resolveContentType(file);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(
                                    new ByteArrayInputStream(bytes),
                                    (long) bytes.length,
                                    -1L
                            )
                            .contentType(contentType)
                            .build()
            );

            return new StoredAudioFile(
                    bucketName,
                    objectKey,
                    file.filename(),
                    contentType,
                    (long) bytes.length
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

    private String buildObjectKey(Long userId, Long audioTextId, String filename) {
        String safeFilename = filename == null
                ? "audio"
                : filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        return "users/" + userId
                + "/texts/" + audioTextId
                + "/" + UUID.randomUUID()
                + "-" + safeFilename;
    }

    private String resolveContentType(FilePart file) {
        MediaType mediaType = file.headers().getContentType();

        if (mediaType == null) {
            return "application/octet-stream";
        }

        return mediaType.toString();
    }
}