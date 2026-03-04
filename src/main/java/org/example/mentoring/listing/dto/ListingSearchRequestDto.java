package org.example.mentoring.listing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListingSearchRequestDto(
        @Min(0) Integer page,
        @Min(1) @Max(50) Integer size,
        String sort,
        String topic,
        String placeType,
        Integer minPrice,
        Integer maxPrice
)
{}
