package org.example.sourceofvoice.DTO.responses.text;

import lombok.Data;
import org.example.sourceofvoice.entities.text.AudioTextBatchStatus;

import java.time.LocalDateTime;

@Data
public class AdminAudioTextBatchDetailsResponse {

    private Long id;
    private Long createdByAdminId;
    private AudioTextBatchStatus status;
    private String languageCode;
    private Integer requestedCount;
    private Integer savedCount;
    private Integer skippedCount;
    private Integer minWords;
    private Integer maxWords;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
