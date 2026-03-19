package org.example.mentoring.reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.example.mentoring.reservation.type.ReservationSort;
import org.example.mentoring.reservation.type.ReservationView;

public record ReservationSearchRequestDto(
        @Min(0) Integer page,
        @Min(1) @Max(10) Integer size,
        ReservationView view,
        ReservationSort sort,
        ReservationFilter filter
        ) {
}
