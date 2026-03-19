package org.example.mentoring.reservation.repository;

import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<ReservationStatus> reservationStatuses);
}
