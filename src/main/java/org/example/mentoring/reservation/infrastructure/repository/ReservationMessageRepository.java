package org.example.mentoring.reservation.infrastructure.repository;

import org.example.mentoring.reservation.domain.ReservationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationMessageRepository extends JpaRepository<ReservationMessage, Long> {
    @EntityGraph(attributePaths = "sender")
    Page<ReservationMessage> findByReservationId(Long reservationId, Pageable pageable);
}
