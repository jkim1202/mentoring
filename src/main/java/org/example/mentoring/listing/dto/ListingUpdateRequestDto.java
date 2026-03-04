package org.example.mentoring.listing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.example.mentoring.listing.entity.PlaceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ListingUpdateRequestDto (
        @Size(min = 1, max = 120)
        String title,
        @Size(min = 1, max = 80)
        String topic,
        @Min(0)
        Integer price,
        PlaceType placeType,
        @Size(max = 255)
        String placeDesc,
        @Size(min = 1, max = 5000)
        String description
){}
