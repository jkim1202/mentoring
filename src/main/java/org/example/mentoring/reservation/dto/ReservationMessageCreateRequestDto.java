package org.example.mentoring.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReservationMessageCreateRequestDto(@NotBlank String content) {
}
