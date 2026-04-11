package org.example.mentoring.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.review.domain.Review;

import java.time.LocalDateTime;

@Schema(description = "리뷰 생성 응답")
public record ReviewCreateResponseDto(
        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,
        @Schema(description = "평점", example = "5")
        Integer rating,
        @Schema(description = "리뷰 내용")
        String content,
        @Schema(description = "생성 시각")
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
