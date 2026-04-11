package org.example.mentoring.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.review.domain.Review;

import java.time.LocalDateTime;

@Schema(description = "리뷰 목록 응답 항목")
public record ReviewSummaryResponseDto(
        Long reviewId,
        Long reviewerUserId,
        String reviewerNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewSummaryResponseDto from(Review review) {
        return new ReviewSummaryResponseDto(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewer().getNickname(),
                review.getRating().intValue(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
