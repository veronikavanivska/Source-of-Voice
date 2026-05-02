package org.example.sourceofvoice.DTO.responses.text;

import lombok.Data;
import org.example.sourceofvoice.entities.text.AudioTextStatus;

import java.time.LocalDateTime;

@Data
public class AudioTextStatusResponse {
    private Long id;
    private AudioTextStatus status;
    private LocalDateTime activatedAt;
    private LocalDateTime disabledAt;
}
