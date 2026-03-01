package org.example.mentoring.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(@Email @NotBlank String email, @NotBlank String password){}
