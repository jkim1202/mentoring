package org.example.mentoring.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.application.entity.ApplicationStatus;

@Schema(description = "신청 상태 변경 응답")
public record ApplicationStatusResponseDto(
        @Schema(description = "신청 ID", example = "1")
        Long id,
        @Schema(description = "변경된 신청 상태", example = "ACCEPTED")
        ApplicationStatus status
) {
}
