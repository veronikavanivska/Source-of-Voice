package org.example.sourceofvoice.entities.text;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("audio_text_batches")
public class AudioTextBatch {

    @Id
    private Long id;

    @Column("created_by_admin_id")
    private Long createdByAdminId;

    @Column("status")
    private AudioTextBatchStatus status;

    @Column("language_code")
    private String languageCode;

    @Column("requested_count")
    private Integer requestedCount;

    @Builder.Default
    @Column("saved_count")
    private Integer savedCount = 0;

    @Builder.Default
    @Column("skipped_count")
    private Integer skippedCount = 0;

    @Column("min_words")
    private Integer minWords;

    @Column("max_words")
    private Integer maxWords;

    @Column("error_message")
    private String errorMessage;

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("finished_at")
    private LocalDateTime finishedAt;
}