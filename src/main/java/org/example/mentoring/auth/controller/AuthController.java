package org.example.mentoring.auth.controller;

import jakarta.validation.Valid;
import org.example.mentoring.auth.dto.*;
import org.example.mentoring.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto authRequestDto) {
        RegisterResponseDto responseDto = authService.register(authRequestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto responseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        RefreshResponseDto responseDto = authService.refreshToken(refreshRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
