package org.example.mentoring.reservation.repository;

import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationRepositoryCustom {
    Page<Reservation> searchByMentorId(ReservationFilter filter, Pageable pageable, Long mentorId);
    Page<Reservation> searchByMenteeId(ReservationFilter filter, Pageable pageable, Long menteeId);

}
