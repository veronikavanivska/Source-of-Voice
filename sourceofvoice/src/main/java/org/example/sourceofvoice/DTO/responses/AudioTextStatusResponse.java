package org.example.sourceofvoice.DTO.responses;

import lombok.Data;
import org.example.sourceofvoice.entities.AudioTextStatus;

import java.time.LocalDateTime;

@Data
public class AudioTextStatusResponse {
    private Long id;
    private AudioTextStatus status;
    private LocalDateTime activatedAt;
    private LocalDateTime disabledAt;
}
