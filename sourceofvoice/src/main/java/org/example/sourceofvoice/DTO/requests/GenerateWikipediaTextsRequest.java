package org.example.sourceofvoice.DTO.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GenerateWikipediaTextsRequest {

    private String languageCode;
    private Integer requestedCount;
    private Integer minWords;
    private Integer maxWords;
    private BigDecimal baseRatePerWord;
    private Boolean activateImmediately;

    private Integer wikipediaFetchLimit;
    private Boolean introOnly;
    private Double minDifficultyScore;
    private Double maxDifficultyScor;
}
