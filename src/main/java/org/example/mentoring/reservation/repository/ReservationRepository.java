package org.example.mentoring.reservation.repository;

import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<ReservationStatus> reservationStatuses);
    List<Reservation> findAllByMentee_IdOrderByCreatedAt(Long menteeId);

    List<Reservation> findAllByMentee_IdOrderByCreatedAtDesc(Long menteeId);

    List<Reservation> findAllByMentor_IdOrderByCreatedAt(Long mentorId);

    List<Reservation> findAllByMentor_IdOrderByCreatedAtDesc(Long mentorId);
}
