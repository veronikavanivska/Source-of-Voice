package org.example.sourceofvoice.DTO.responses.text;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAudioTextDetailsResponse {

    private Long id;
    private String languageCode;
    private String sourceTitle;
    private String content;
    private Integer wordCount;
    private Double difficultyScore;
    private Integer estimatedReadingSeconds;
    private BigDecimal basePrice;
}
