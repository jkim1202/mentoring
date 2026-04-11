package org.example.mentoring.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.ListingStatus;
import org.example.mentoring.listing.entity.PlaceType;

@Schema(description = "등록글 상세 응답")
public record ListingResponseDto(
        @Schema(description = "등록글 ID", example = "1")
        Long id,
        @Schema(description = "등록글 제목", example = "Spring 실무 멘토링")
        String title,
        @Schema(description = "주제", example = "Spring")
        String topic,
        @Schema(description = "가격", example = "60000")
        Integer price,
        @Schema(description = "장소 유형", example = "ONLINE")
        PlaceType placeType,
        @Schema(description = "상세 설명")
        String description,
        @Schema(description = "장소 설명")
        String placeDesc,
        @Schema(description = "등록글 상태", example = "ACTIVE")
        ListingStatus status
) {
    public static ListingResponseDto from(Listing listing) {
        return new ListingResponseDto(
                listing.getId(),
                listing.getTitle(),
                listing.getTopic(),
                listing.getPrice(),
                listing.getPlaceType(),
                listing.getDescription(),
                listing.getPlaceDesc(),
                listing.getStatus()
        );
    }
}
