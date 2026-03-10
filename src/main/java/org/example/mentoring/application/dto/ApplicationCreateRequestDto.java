package org.example.mentoring.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequestDto(@NotNull Long listingId, @NotNull Long slotId, @Size(max = 1000) String message) {
}
