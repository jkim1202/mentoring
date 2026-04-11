package org.example.mentoring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public record RegisterRequestDto(
        @Schema(description = "회원 이메일", example = "mentee@example.com")
        @Email @NotBlank String email,
        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank String password
) {
}
