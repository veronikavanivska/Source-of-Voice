package org.example.sourceofvoice.DTO.responses.text;

import lombok.Data;
import org.example.sourceofvoice.entities.text.AudioTextStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminAudioTextDetailsResponse {

    private Long id;
    private Long batchId;
    private AudioTextStatus status;
    private String languageCode;
    private Long sourcePageId;
    private String sourceTitle;
    private String sourceUrl;
    private String content;
    private Integer wordCount;
    private Integer characterCount;
    private Double difficultyScore;
    private Integer estimatedReadingSeconds;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime disabledAt;

}