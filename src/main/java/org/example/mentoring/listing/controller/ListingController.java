package org.example.mentoring.listing.controller;

import jakarta.validation.Valid;
import org.example.mentoring.listing.dto.ListingResponseDto;
import org.example.mentoring.listing.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.dto.ListingSummaryResponseDto;
import org.example.mentoring.listing.dto.ListingUpdateRequestDto;
import org.example.mentoring.listing.service.ListingService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ListingController {
    private final ListingService listingService;

    @Autowired
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings/{id}")
    public ResponseEntity<ListingResponseDto> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    @GetMapping("/listings")
    public ResponseEntity<Page<ListingSummaryResponseDto>> getListings(
            @ModelAttribute ListingSearchRequestDto request
    ) {
        Page<ListingSummaryResponseDto> result = listingService.getListings(request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/listings/{id}")
    public ResponseEntity<ListingResponseDto> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequestDto request,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.updateListing(id, userDetails.getId(), request);
        return ResponseEntity.ok(result);
    }


}
