package org.example.mentoring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.user.entity.UserStatus;

@Schema(description = "회원가입 응답")
public record RegisterResponseDto(
        @Schema(description = "가입한 이메일", example = "mentee@example.com")
        String email,
        @Schema(description = "회원 상태", example = "ACTIVE")
        UserStatus userStatus
) {
}
