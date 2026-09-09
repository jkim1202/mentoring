package org.example.mentoring.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.reservation.domain.ReservationStatus;

public record ReservationStatusUpdateRequestDto(@NotNull ReservationStatus status) {

}
