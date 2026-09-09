package org.example.mentoring.listing.presentation.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.listing.domain.ListingStatus;

public record ListingStatusUpdateRequestDto(@NotNull ListingStatus status) {
}
