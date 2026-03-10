package org.example.mentoring.listing.service;

import jakarta.transaction.Transactional;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.dto.*;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.ListingStatus;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Autowired
    public ListingService(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ListingResponseDto createListing(Long loginId, ListingCreateRequestDto req) {
        User mentor = userRepository.findById(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String placeDesc = req.placeType() == PlaceType.ONLINE ? null : req.placeDesc();
        if(req.placeType() == PlaceType.OFFLINE && (req.placeDesc() == null || req.placeDesc().isBlank()))
            throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT);
        Listing listing = Listing.builder()
                .mentor(mentor)
                .title(req.title())
                .topic(req.topic())
                .price(req.price())
                .placeType(req.placeType())
                .placeDesc(placeDesc)
                .description(req.description())
                .build();

        return ListingResponseDto.from(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponseDto getListing(Long id) {
        return ListingResponseDto
                .from(listingRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.LISTING_NOT_FOUND)
                        )
                );
    }

    @Transactional
    public Page<ListingSummaryResponseDto> getListings(ListingSearchRequestDto req) {
        if (req.maxPrice() != null && req.minPrice() != null && req.maxPrice() < req.minPrice())
            throw new BusinessException(ErrorCode.LISTING_INVALID_PRICE_RANGE);
        int page = req.page() == null ? 0 : req.page();
        int size = req.size() == null ? 10 : req.size();
        String sort = req.sort() == null ? "LATEST" : req.sort();

        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        return listingRepository.search(req, pageable)
                .map(listing -> new ListingSummaryResponseDto(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getTopic(),
                        listing.getPrice(),
                        listing.getAvgRating(),
                        listing.getReviewCount()
                ));
    }


    @Transactional
    public ListingResponseDto updateListing(Long listingId, Long loginId, ListingUpdateRequestDto req) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));

        if (!listing.getMentor().getId().equals(loginId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.LISTING_NOT_EDITABLE);
        }

        applyUpdates(listing, req);

        return ListingResponseDto.from(listing);
    }

    @Transactional
    public ListingResponseDto updateStatus(
            Long listingId,
            Long loginId,
            ListingStatusUpdateRequestDto req
    ) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));

        if (!listing.getMentor().getId().equals(loginId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }

        listing.updateStatus(req.status());
        return ListingResponseDto.from(listing);
    }

    private Sort toSort(String sort) {
        return switch (sort.toUpperCase()) {
            case "RATING" -> Sort.by(
                    Sort.Order.desc("avgRating"),
                    Sort.Order.desc("reviewCount")
            );
            case "REVIEWS" -> Sort.by(
                    Sort.Order.desc("reviewCount"),
                    Sort.Order.desc("avgRating")
            );
            case "PRICE_ASC" -> Sort.by(Sort.Direction.ASC, "price");
            case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
    private void applyUpdates(Listing listing, ListingUpdateRequestDto req) {
        listing.changeTitle(req.title());
        listing.changeTopic(req.topic());
        listing.changePrice(req.price());
        listing.changePlace(req.placeType(), req.placeDesc());
        listing.changeDescription(req.description());
    }
}
