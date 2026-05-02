CREATE TABLE audio_submissions (
                                   id BIGSERIAL PRIMARY KEY,

                                   user_id BIGINT NOT NULL,
                                   audio_text_id BIGINT NOT NULL,

                                   status VARCHAR(50) NOT NULL,

                                   bucket_name VARCHAR(255) NOT NULL,
                                   object_key VARCHAR(1000) NOT NULL,
                                   original_file_name VARCHAR(500),
                                   content_type VARCHAR(100),
                                   file_size_bytes BIGINT NOT NULL,

                                   speechmatics_job_id VARCHAR(255),
                                   transcript_text TEXT,
                                   correctness_score DOUBLE PRECISION,

                                   payout_amount NUMERIC(10, 2),

                                   submitted_at TIMESTAMP NOT NULL,
                                   transcribed_at TIMESTAMP,

                                   assigned_reviewer_id BIGINT,
                                   assigned_at TIMESTAMP,

                                   reviewed_at TIMESTAMP,
                                   reviewed_by BIGINT,

                                   CONSTRAINT fk_audio_submission_text
                                       FOREIGN KEY (audio_text_id)
                                           REFERENCES audio_texts(id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT chk_audio_submission_status
                                       CHECK (status IN (
                                                         'SUBMITTED',
                                                         'TRANSCRIBING',
                                                         'AUTO_REJECTED',
                                                         'NEEDS_REVIEW',
                                                         'IN_REVIEW',
                                                         'APPROVED_FOR_PAYMENT',
                                                         'REJECTED_BY_REVIEWER',
                                                         'APPROVED_BY_REVIEWER',
                                                         'TRANSCRIPTION_FAILED'
                                           )),

                                   CONSTRAINT chk_audio_submission_score
                                       CHECK (
                                           correctness_score IS NULL
                                               OR correctness_score >= 0
                                               AND correctness_score <= 100
                                           ),

                                   CONSTRAINT chk_audio_submission_file_size_positive
                                       CHECK (file_size_bytes > 0),

                                   CONSTRAINT chk_audio_submission_payout_non_negative
                                       CHECK (
                                           payout_amount IS NULL
                                               OR payout_amount >= 0
                                           )
);

CREATE INDEX idx_audio_submissions_user_id
    ON audio_submissions(user_id);

CREATE INDEX idx_audio_submissions_text_id
    ON audio_submissions(audio_text_id);

CREATE INDEX idx_audio_submissions_status
    ON audio_submissions(status);

CREATE INDEX idx_audio_submissions_speechmatics_job_id
    ON audio_submissions(speechmatics_job_id);

CREATE INDEX idx_audio_submissions_assigned_reviewer_id
    ON audio_submissions(assigned_reviewer_id);

CREATE INDEX idx_audio_submissions_submitted_at
    ON audio_submissions(submitted_at);
