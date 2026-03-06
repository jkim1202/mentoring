package org.example.mentoring.listing.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.listing.entity.ListingStatus;

public record ListingStatusUpdateRequestDto(@NotNull ListingStatus status) {
}
