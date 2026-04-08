package org.example.mentoring.review.dto;

import org.example.mentoring.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewCreateResponseDto(
        Long reviewId,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewCreateResponseDto from(Review review) {
        return new ReviewCreateResponseDto(
                review.getId(),
                review.getRating().intValue(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
