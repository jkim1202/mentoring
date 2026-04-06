package org.example.mentoring.review.dto;

import org.example.mentoring.review.domain.Review;

import java.time.LocalDateTime;

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
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
