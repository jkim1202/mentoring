package org.example.mentoring.reservation.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.reservation.entity.ReservationStatus;

public record ReservationStatusUpdateRequestDto(@NotNull ReservationStatus status) {

}
