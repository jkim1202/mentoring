package org.example.mentoring.mentoringapplication.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.mentoringapplication.domain.MentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationStatus;

@Schema(description = "신청 생성 응답")
public record ApplicationCreateResponseDto(
        @Schema(description = "신청 ID", example = "1")
        Long id,
        @Schema(description = "신청 상태", example = "APPLIED")
        ApplicationStatus status,
        @Schema(description = "신청 메시지")
        String message) {
    public static ApplicationCreateResponseDto from(MentoringApplication application) {
        return new ApplicationCreateResponseDto(application.getId(), application.getStatus(), application.getMessage());
    }
}
