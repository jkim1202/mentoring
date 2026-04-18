package org.example.mentoring.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "내 등록글 목록 응답 항목")
public record MyListingSummaryResponseDto(
        @Schema(description = "등록글 ID", example = "1")
        Long id,
        @Schema(description = "등록글 제목", example = "Spring 실무 멘토링")
        String title,
        @Schema(description = "주제", example = "Spring")
        String topic,
        @Schema(description = "가격", example = "60000")
        Integer price,
        @Schema(description = "등록글 상태", example = "ACTIVE")
        ListingStatus status,
        @Schema(description = "평균 평점", example = "4.80")
        BigDecimal avgRating,
        @Schema(description = "리뷰 수", example = "12")
        Integer reviewCount,
        @Schema(description = "생성 시각", example = "2026-04-18T10:00:00")
        LocalDateTime createdAt
) {
    public static MyListingSummaryResponseDto from(Listing listing) {
        return new MyListingSummaryResponseDto(
                listing.getId(),
                listing.getTitle(),
                listing.getTopic(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getAvgRating(),
                listing.getReviewCount(),
                listing.getCreatedAt()
        );
    }
}
