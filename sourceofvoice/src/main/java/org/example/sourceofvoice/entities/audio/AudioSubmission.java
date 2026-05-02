package org.example.sourceofvoice.entities.audio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audio_submissions")
public class AudioSubmission {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("audio_text_id")
    private Long audioTextId;

    @Column("status")
    private AudioSubmissionStatus status;

    @Column("bucket_name")
    private String bucketName;

    @Column("object_key")
    private String objectKey;

    @Column("original_file_name")
    private String originalFileName;

    @Column("content_type")
    private String contentType;

    @Column("file_size_bytes")
    private Long fileSizeBytes;

    @Column("speechmatics_job_id")
    private String speechmaticsJobId;

    @Column("transcript_text")
    private String transcriptText;

    @Column("correctness_score")
    private Double correctnessScore;

    @Column("payout_amount")
    private BigDecimal payoutAmount;

    @Column("submitted_at")
    private LocalDateTime submittedAt;

    @Column("transcribed_at")
    private LocalDateTime transcribedAt;

    /*
     * Reviewer assignment
     */
    @Column("assigned_reviewer_id")
    private Long assignedReviewerId;

    @Column("assigned_at")
    private LocalDateTime assignedAt;

    @Column("reviewed_at")
    private LocalDateTime reviewedAt;

    @Column("reviewed_by")
    private Long reviewedBy;

}
