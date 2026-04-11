package org.example.mentoring.listing.controller;

import jakarta.validation.Valid;
import org.example.mentoring.listing.dto.ListingCreateRequestDto;
import org.example.mentoring.listing.dto.ListingResponseDto;
import org.example.mentoring.listing.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.dto.ListingStatusUpdateRequestDto;
import org.example.mentoring.listing.dto.ListingSummaryResponseDto;
import org.example.mentoring.listing.dto.ListingUpdateRequestDto;
import org.example.mentoring.listing.service.ListingService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ListingResponseDto> createListing(
            @Valid @RequestBody ListingCreateRequestDto request,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.createListing(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDto> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    @GetMapping
    public ResponseEntity<Page<ListingSummaryResponseDto>> getListings(
            @ModelAttribute ListingSearchRequestDto request
    ) {
        Page<ListingSummaryResponseDto> result = listingService.getListings(request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ListingResponseDto> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequestDto request,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.updateListing(id, userDetails.getId(), request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ListingResponseDto> updateListingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingStatusUpdateRequestDto request,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.updateStatus(id, userDetails.getId(), request);
        return ResponseEntity.ok(result);
    }

}
