package org.example.mentoring.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.review.domain.Review;

import java.time.LocalDateTime;

@Schema(description = "리뷰 상세 응답")
public record ReviewDetailResponseDto(
        Long reviewId,
        Long reservationId,
        Long listingId,
        String listingTitle,
        Long reviewerUserId,
        String reviewerNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewDetailResponseDto from(Review review) {
        return new ReviewDetailResponseDto(
                review.getId(),
                review.getReservation().getId(),
                review.getListing().getId(),
                review.getListing().getTitle(),
                review.getReviewer().getId(),
                review.getReviewer().getNickname(),
                review.getRating().intValue(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
