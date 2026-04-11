package org.example.mentoring.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리뷰 목록 조회 요청")
public record ReviewSearchRequestDto(
        @Schema(description = "등록글 ID", example = "1")
        @NotNull Long listingId,
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page,
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(20) Integer size
) {
}
