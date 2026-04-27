package org.example.mentoring.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "내 찜 목록 조회 요청")
public record LikeSearchRequestDto(
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page,
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(50) Integer size
) {
}
