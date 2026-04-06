package org.example.mentoring.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewSearchRequestDto(
        @NotNull Long listingId,
        @Min(0) Integer page,
        @Min(1) @Max(20) Integer size
) {
}
