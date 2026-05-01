package org.example.sourceofvoice.DTO.responses;

import lombok.Data;
import org.example.sourceofvoice.entities.AudioTextBatchStatus;

import java.time.LocalDateTime;

@Data
public class GenerateWikipediaTextsResponse {
    private Long batchId;
    private AudioTextBatchStatus status;
    private Integer requestedCount;
    private Integer savedCount;
    private Integer skippedCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
}
