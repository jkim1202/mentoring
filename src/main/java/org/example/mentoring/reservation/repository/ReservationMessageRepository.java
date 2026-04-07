package org.example.mentoring.reservation.repository;

import org.example.mentoring.reservation.entity.ReservationMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationMessageRepository extends JpaRepository<ReservationMessage, Long> {
    @EntityGraph(attributePaths = "sender")
    List<ReservationMessage> findByReservationIdOrderByCreatedAtAsc(Long reservationId);
}
