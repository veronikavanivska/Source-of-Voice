package org.example.sourceofvoice.DTO.responses.audio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioFileResponse {
    private byte[] bytes;
    private String contentType;
}
