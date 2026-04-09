package org.example.mentoring.reservation.repository;

import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    Optional<Reservation> findByApplicationId(Long applicationId);
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<ReservationStatus> reservationStatuses);

    @Query("""
            select r from Reservation r
            join fetch r.listing
            join fetch r.mentor
            join fetch r.mentee
            join fetch r.slot
            where r.id = :reservationId
            """)
    Optional<Reservation> findDetailById(@Param("reservationId") Long reservationId);

    @Query("""
            select r from Reservation r
            join fetch r.slot
            where r.status = org.example.mentoring.reservation.entity.ReservationStatus.PENDING_PAYMENT
                         and (r.createdAt <= :paymentDeadLine or r.startAt <= :now)
            """)
    List<Reservation> findPendingReservationsToExpire(
            @Param("now") LocalDateTime now,
            @Param("paymentDeadLine") LocalDateTime paymentDeadLine
    );
}
