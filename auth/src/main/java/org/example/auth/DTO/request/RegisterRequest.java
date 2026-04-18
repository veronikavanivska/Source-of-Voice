package org.example.auth.DTO.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
}
