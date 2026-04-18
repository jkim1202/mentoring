package org.example.mentoring.user.service;

import org.example.mentoring.listing.dto.MyListingSearchRequestDto;
import org.example.mentoring.listing.dto.MyListingSummaryResponseDto;
import org.example.mentoring.listing.service.ListingService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

    private final ListingService listingService;

    public MyPageService(ListingService listingService) {
        this.listingService = listingService;
    }

    @Transactional(readOnly = true)
    public Page<MyListingSummaryResponseDto> getMyListings(Long loginId, MyListingSearchRequestDto requestDto) {
        return listingService.getMyListings(loginId, requestDto);
    }
}
