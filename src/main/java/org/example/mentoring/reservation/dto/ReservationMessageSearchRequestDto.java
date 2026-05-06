package org.example.mentoring.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "예약 메시지 요청 항목")
public record ReservationMessageSearchRequestDto(
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(20) Integer size,
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page
) {
}
