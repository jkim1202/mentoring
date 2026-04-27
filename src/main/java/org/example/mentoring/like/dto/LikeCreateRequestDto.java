package org.example.mentoring.like.dto;

import jakarta.validation.constraints.NotNull;

public record LikeCreateRequestDto(@NotNull Long listingId) {
}
