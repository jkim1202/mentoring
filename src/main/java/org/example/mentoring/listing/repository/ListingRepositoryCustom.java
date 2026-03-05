package org.example.mentoring.listing.repository;

import org.example.mentoring.listing.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingRepositoryCustom {
    Page<Listing> search(ListingSearchRequestDto req, Pageable pageable);
}
