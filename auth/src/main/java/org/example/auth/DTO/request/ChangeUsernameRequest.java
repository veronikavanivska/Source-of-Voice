package org.example.auth.DTO.request;

import lombok.Data;

@Data
public class ChangeUsernameRequest {
    private Long userId;
    private String username;
}
