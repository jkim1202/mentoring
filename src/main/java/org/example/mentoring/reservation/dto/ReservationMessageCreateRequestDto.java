package org.example.mentoring.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "예약 메시지 생성 요청")
public record ReservationMessageCreateRequestDto(
        @Schema(description = "메시지 내용", example = "입금 완료했습니다. 확인 부탁드립니다.")
        @NotBlank String content
) {
}
