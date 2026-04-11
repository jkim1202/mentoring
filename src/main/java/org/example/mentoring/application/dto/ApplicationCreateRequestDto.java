package org.example.mentoring.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신청 생성 요청")
public record ApplicationCreateRequestDto(
        @Schema(description = "등록글 ID", example = "1")
        @NotNull Long listingId,
        @Schema(description = "신청할 슬롯 ID", example = "10")
        @NotNull Long slotId,
        @Schema(description = "신청 메시지", example = "이번 주 토요일 오후에 상담 받고 싶습니다.")
        @Size(max = 1000) String message
) {
}
