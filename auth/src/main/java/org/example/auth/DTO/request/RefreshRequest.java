package org.example.auth.DTO.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
