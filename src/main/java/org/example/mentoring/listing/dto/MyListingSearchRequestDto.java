package org.example.mentoring.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.mentoring.listing.entity.ListingStatus;

@Schema(description = "내 등록글 목록 조회 요청")
public record MyListingSearchRequestDto(
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page,
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(50) Integer size,
        @Schema(description = "정렬 기준", example = "LATEST")
        String sort,
        @Schema(description = "등록글 상태 필터", example = "ACTIVE")
        ListingStatus status
) {
}
