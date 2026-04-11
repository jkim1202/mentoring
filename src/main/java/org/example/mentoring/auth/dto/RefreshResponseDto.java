package org.example.mentoring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record RefreshResponseDto(
        @Schema(description = "새 access token")
        String accessToken,
        @Schema(description = "새 refresh token")
        String refreshToken
) {
}
