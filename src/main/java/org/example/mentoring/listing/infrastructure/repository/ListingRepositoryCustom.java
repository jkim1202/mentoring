package org.example.mentoring.listing.infrastructure.repository;

import org.example.mentoring.listing.presentation.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.domain.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingRepositoryCustom {
    Page<Listing> search(ListingSearchRequestDto req, Pageable pageable);
}
