package org.example.mentoring.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.like.entity.Like;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "내 찜 목록 응답 항목")
public record LikeSummaryResponseDto(
        @Schema(description = "등록글 ID", example = "1")
        Long listingId,
        @Schema(description = "등록글 제목", example = "Spring 실무 멘토링")
        String title,
        @Schema(description = "주제", example = "Spring")
        String topic,
        @Schema(description = "가격", example = "60000")
        Integer price,
        @Schema(description = "평균 평점", example = "4.80")
        BigDecimal avgRating,
        @Schema(description = "리뷰 수", example = "12")
        Integer reviewCount,
        @Schema(description = "찜한 시각", example = "2026-04-27T13:00:00")
        LocalDateTime likedAt,
        @Schema(description = "찜 여부", example = "true")
        boolean liked
) {
    public static LikeSummaryResponseDto from(Like like) {
        return new LikeSummaryResponseDto(
                like.getListing().getId(),
                like.getListing().getTitle(),
                like.getListing().getTopic(),
                like.getListing().getPrice(),
                like.getListing().getAvgRating(),
                like.getListing().getReviewCount(),
                like.getCreatedAt(),
                true
        );
    }
}
