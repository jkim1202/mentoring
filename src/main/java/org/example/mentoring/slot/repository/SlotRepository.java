package org.example.mentoring.slot.repository;

import jakarta.persistence.LockModeType;
import org.example.mentoring.slot.entity.Slot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;


public interface SlotRepository extends JpaRepository<Slot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :slotId")
    Optional<Slot> findByIdForUpdate(@Param("slotId") Long slotId);

    @Query("""
            select s from Slot s
            where s.listing.id = :listingId
              and (:from is null or s.startAt >= :from)
              and (:to is null or s.endAt <= :to)
            """)
    Page<Slot> searchByListingId(
            @Param("listingId") Long listingId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
