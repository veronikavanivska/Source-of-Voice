package org.example.sourceofvoice.entities.audio;

public enum AudioSubmissionStatus {
    SUBMITTED,
    TRANSCRIPTION_REQUESTED,
    TRANSCRIBING,
    AUTO_REJECTED,
    NEEDS_REVIEW,
    IN_REVIEW,
    APPROVED_FOR_PAYMENT,
    REJECTED_FOR_PAYMENT,
    APPROVED_BY_REVIEWER,
    TRANSCRIPTION_FAILED
}
