package org.example.mentoring.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리뷰 생성 요청")
public record ReviewCreateRequestDto(
        @Schema(description = "완료된 예약 ID", example = "10")
        @NotNull Long reservationId,
        @Schema(description = "평점(1~5)", example = "5")
        @NotNull @Min(1) @Max(5) Integer rating,
        @Schema(description = "리뷰 내용", example = "시간 약속이 정확하고 설명이 명확했습니다.")
        String content
) {
}
