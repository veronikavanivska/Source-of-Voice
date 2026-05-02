package org.example.sourceofvoice.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredAudioFile {
    private String bucketName;
    private String objectKey;
    private String originalFileName;
    private String contentType;
    private long fileSizeBytes;
}
