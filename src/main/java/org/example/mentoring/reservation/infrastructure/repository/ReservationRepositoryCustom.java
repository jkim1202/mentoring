package org.example.mentoring.reservation.infrastructure.repository;

import org.example.mentoring.reservation.domain.Reservation;
import org.example.mentoring.reservation.domain.ReservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationRepositoryCustom {
    Page<Reservation> searchByMentorId(ReservationFilter filter, Pageable pageable, Long mentorId);
    Page<Reservation> searchByMenteeId(ReservationFilter filter, Pageable pageable, Long menteeId);

}
