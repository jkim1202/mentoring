package org.example.mentoring.like.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record LikeCreateRequestDto(@NotNull Long listingId) {
}
