package org.example.sourceofvoice.DTO.responses.audio;

import lombok.Data;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReviewerAudioSubmissionDetailsResponse {

    private Long id;
    private Long userId;
    private Long audioTextId;

    private AudioSubmissionStatus status;

    private String sourceTitle;
    private String originalText;
    private String transcriptText;

    private String audioUrl;

    private Double correctnessScore;
    private BigDecimal payoutAmount;

    private Long assignedReviewerId;
    private LocalDateTime assignedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime transcribedAt;
}