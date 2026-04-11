package org.example.mentoring.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.mentoring.listing.entity.PlaceType;

@Schema(description = "등록글 목록 조회 요청")
public record ListingSearchRequestDto(
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page,
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(50) Integer size,
        @Schema(description = "정렬 기준", example = "LATEST")
        String sort,
        @Schema(description = "주제 검색어", example = "Spring")
        String topic,
        @Schema(description = "장소 유형", example = "ONLINE")
        PlaceType placeType,
        @Schema(description = "최소 가격", example = "30000")
        Integer minPrice,
        @Schema(description = "최대 가격", example = "100000")
        Integer maxPrice
)
{}
