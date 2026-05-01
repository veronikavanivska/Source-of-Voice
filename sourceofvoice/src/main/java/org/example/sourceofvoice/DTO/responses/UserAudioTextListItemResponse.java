package org.example.sourceofvoice.DTO.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAudioTextListItemResponse {

    private Long id;
    private String languageCode;
    private String sourceTitle;
    private Integer wordCount;
    private Double difficultyScore;
    private Integer estimatedReadingSeconds;
    private BigDecimal basePrice;
}