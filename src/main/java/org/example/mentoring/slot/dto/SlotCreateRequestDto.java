package org.example.mentoring.slot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "슬롯 생성 요청")
public record SlotCreateRequestDto(
        @Schema(description = "시작 시각", example = "2026-04-20T14:00:00")
        @NotNull
        LocalDateTime startAt,
        @Schema(description = "종료 시각", example = "2026-04-20T15:00:00")
        @NotNull
        LocalDateTime endAt
) {
}
