package org.example.auth.controllers;

import org.example.auth.DTO.request.*;
import org.example.auth.DTO.response.AuthResponse;
import org.example.auth.services.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController{

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public String registration(@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){
      return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request){
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("X-User-Id") Long userId){
        return authService.logout(userId);
    }

    @PostMapping("/changeEmail")
    public String changeEmail(@RequestHeader("X-User-Id") Long userId, @RequestBody EmailChangeRequest request){
        request.setUserId(userId);
        return authService.changeEmail(request);
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestHeader("X-User-Id") Long userId, @RequestBody ChangePasswordRequest request){
        request.setUserId(userId);
        return authService.changePassword(request);
    }

    @PostMapping("/changeUsername")
    public String changeUsername(@RequestHeader("X-User-Id") Long userId, @RequestBody ChangeUsernameRequest request){
        request.setUserId(userId);
        return authService.changeUsername(request);
    }

}
