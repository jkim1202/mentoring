package org.example.mentoring.listing.dto;

import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;

public record ListingResponseDto(
        Long id,
        String title,
        String topic,
        Integer price,
        PlaceType placeType,
        String description,
        String placeDesc
) {
    public static ListingResponseDto from(Listing listing) {
        return new ListingResponseDto(
                listing.getId(),
                listing.getTitle(),
                listing.getTopic(),
                listing.getPrice(),
                listing.getPlaceType(),
                listing.getDescription(),
                listing.getPlaceDesc()
        );
    }
}
