package org.example.mentoring.listing.infrastructure.repository;

import org.example.mentoring.listing.domain.ListingStatus;
import org.example.mentoring.listing.domain.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, ListingRepositoryCustom {
    Page<Listing> findByMentorId(Long mentorId, Pageable pageable);
    Page<Listing> findByMentorIdAndStatus(Long mentorId, ListingStatus status, Pageable pageable);
}
