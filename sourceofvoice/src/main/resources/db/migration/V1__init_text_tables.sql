CREATE TABLE audio_text_batches (
                                    id BIGSERIAL PRIMARY KEY,

                                    created_by_admin_id BIGINT NOT NULL,

                                    status VARCHAR(50) NOT NULL,
                                    language_code VARCHAR(10) NOT NULL,

                                    requested_count INTEGER NOT NULL,
                                    saved_count INTEGER NOT NULL DEFAULT 0,
                                    skipped_count INTEGER NOT NULL DEFAULT 0,

                                    min_words INTEGER NOT NULL,
                                    max_words INTEGER NOT NULL,

                                    error_message TEXT,

                                    started_at TIMESTAMP NOT NULL,
                                    finished_at TIMESTAMP,

                                    CONSTRAINT chk_audio_text_batch_status
                                        CHECK (status IN ('STARTED', 'COMPLETED', 'FAILED')),

                                    CONSTRAINT chk_audio_text_batch_requested_count_positive
                                        CHECK (requested_count > 0),

                                    CONSTRAINT chk_audio_text_batch_saved_count_non_negative
                                        CHECK (saved_count >= 0),

                                    CONSTRAINT chk_audio_text_batch_skipped_count_non_negative
                                        CHECK (skipped_count >= 0),

                                    CONSTRAINT chk_audio_text_batch_word_range_valid
                                        CHECK (min_words > 0 AND max_words >= min_words)
);

CREATE TABLE audio_texts (
                             id BIGSERIAL PRIMARY KEY,

                             batch_id BIGINT,

                             status VARCHAR(50) NOT NULL,

                             language_code VARCHAR(10) NOT NULL,

                             source_page_id BIGINT NOT NULL,
                             source_title VARCHAR(500) NOT NULL,
                             source_url VARCHAR(1000),

                             content TEXT NOT NULL,

                             word_count INTEGER NOT NULL,
                             character_count INTEGER NOT NULL,
                             difficulty_score DOUBLE PRECISION NOT NULL,
                             estimated_reading_seconds INTEGER NOT NULL,

                             base_price NUMERIC(10, 2) NOT NULL,

                             created_at TIMESTAMP NOT NULL,
                             activated_at TIMESTAMP,
                             disabled_at TIMESTAMP,


                             CONSTRAINT fk_audio_text_batch
                                 FOREIGN KEY (batch_id)
                                     REFERENCES audio_text_batches(id)
                                     ON DELETE SET NULL,

                             CONSTRAINT chk_audio_text_status
                                 CHECK (status IN ('GENERATED', 'ACTIVE', 'DISABLED', 'ARCHIVED')),

                             CONSTRAINT chk_audio_text_word_count_positive
                                 CHECK (word_count > 0),

                             CONSTRAINT chk_audio_text_character_count_positive
                                 CHECK (character_count > 0),

                             CONSTRAINT chk_audio_text_difficulty_score_positive
                                 CHECK (difficulty_score > 0),

                             CONSTRAINT chk_audio_text_estimated_seconds_positive
                                 CHECK (estimated_reading_seconds > 0),

                             CONSTRAINT chk_audio_text_base_price_non_negative
                                 CHECK (base_price >= 0),

                             CONSTRAINT uk_audio_text_source_page_language
                                 UNIQUE (source_page_id, language_code)
);

CREATE INDEX idx_audio_texts_status
    ON audio_texts(status);

CREATE INDEX idx_audio_texts_language_status
    ON audio_texts(language_code, status);

CREATE INDEX idx_audio_texts_batch_id
    ON audio_texts(batch_id);

CREATE INDEX idx_audio_text_batches_status
    ON audio_text_batches(status);

CREATE INDEX idx_audio_text_batches_created_by_admin_id
    ON audio_text_batches(created_by_admin_id);