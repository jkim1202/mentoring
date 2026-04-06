package org.example.mentoring.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequestDto(
        @NotNull Long reservationId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String content
) {
}
