package org.example.mentoring.listing.repository;

import jakarta.persistence.LockModeType;
import org.example.mentoring.listing.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SlotRepository extends JpaRepository<Slot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :slotId")
    Optional<Slot> findByIdForUpdate(@Param("slotId") Long slotId);
}
