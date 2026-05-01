package org.example.sourceofvoice.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("audio_texts")
public class AudioText {

    @Id
    private Long id;

    @Column("batch_id")
    private Long batchId;

    @Column("status")
    private AudioTextStatus status;

    @Column("language_code")
    private String languageCode;

    @Column("source_page_id")
    private Long sourcePageId;

    @Column("source_title")
    private String sourceTitle;

    @Column("source_url")
    private String sourceUrl;

    @Column("content")
    private String content;

    @Column("word_count")
    private Integer wordCount;

    @Column("character_count")
    private Integer characterCount;

    @Column("difficulty_score")
    private Double difficultyScore;

    @Column("estimated_reading_seconds")
    private Integer estimatedReadingSeconds;

    @Column("base_price")
    private BigDecimal basePrice;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("activated_at")
    private LocalDateTime activatedAt;

    @Column("disabled_at")
    private LocalDateTime disabledAt;

}