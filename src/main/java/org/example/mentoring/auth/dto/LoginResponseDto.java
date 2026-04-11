package org.example.mentoring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponseDto(
        @Schema(description = "Access token")
        String accessToken,
        @Schema(description = "Refresh token")
        String refreshToken
) {
}
