package org.example.mentoring.slot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Schema(description = "슬롯 목록 조회 요청")
public record SlotSearchRequestDto(
        @Schema(description = "시작 조회 범위", example = "2026-04-20T00:00:00")
        java.time.LocalDateTime from,
        @Schema(description = "종료 조회 범위", example = "2026-04-30T23:59:59")
        java.time.LocalDateTime to,
        @Schema(description = "페이지 번호", example = "0")
        @Min(0)
        Integer page,
        @Schema(description = "페이지 크기", example = "20")
        @Positive
        Integer size
) {
}
