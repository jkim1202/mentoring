package org.example.mentoring.listing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.mentoring.listing.entity.PlaceType;

public record ListingCreateRequestDto(
        @NotBlank
        @Size(max = 120)
        String title,
        @NotBlank
        @Size(max = 80)
        String topic,
        @NotNull
        @Min(0)
        Integer price,
        @NotNull
        PlaceType placeType,
        @Size(max = 255)
        String placeDesc,
        @NotBlank
        @Size(max = 5000)
        String description
) {
}
