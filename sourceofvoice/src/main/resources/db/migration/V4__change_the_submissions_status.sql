ALTER TABLE audio_submissions
DROP CONSTRAINT chk_audio_submission_status;

ALTER TABLE audio_submissions
    ADD CONSTRAINT chk_audio_submission_status
        CHECK (status IN (
                          'SUBMITTED',
                          'TRANSCRIPTION_REQUESTED',
                          'TRANSCRIBING',
                          'AUTO_REJECTED',
                          'NEEDS_REVIEW',
                          'IN_REVIEW',
                          'APPROVED_FOR_PAYMENT',
                          'REJECTED_FOR_PAYMENT',
                          'TRANSCRIPTION_FAILED'
            ));