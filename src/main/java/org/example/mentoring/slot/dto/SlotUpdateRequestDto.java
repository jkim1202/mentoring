package org.example.mentoring.slot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "슬롯 수정 요청")
public record SlotUpdateRequestDto(
        @Schema(description = "수정할 시작 시각", example = "2026-04-20T16:00:00")
        @NotNull
        LocalDateTime startAt,
        @Schema(description = "수정할 종료 시각", example = "2026-04-20T17:00:00")
        @NotNull
        LocalDateTime endAt
) {
}
