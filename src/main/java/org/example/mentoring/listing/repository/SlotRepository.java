package org.example.mentoring.listing.repository;

import org.example.mentoring.listing.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot, Long> {

}
