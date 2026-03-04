package org.example.mentoring.listing.repository;

import org.example.mentoring.listing.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {
}
