package org.example.mentoring.review.application;

import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.listing.domain.Listing;
import org.example.mentoring.reservation.domain.Reservation;
import org.example.mentoring.reservation.domain.ReservationStatus;
import org.example.mentoring.reservation.infrastructure.repository.ReservationRepository;
import org.example.mentoring.review.domain.Review;
import org.example.mentoring.review.presentation.dto.ReviewCreateRequestDto;
import org.example.mentoring.review.presentation.dto.ReviewCreateResponseDto;
import org.example.mentoring.review.presentation.dto.ReviewDetailResponseDto;
import org.example.mentoring.review.presentation.dto.ReviewSearchRequestDto;
import org.example.mentoring.review.presentation.dto.ReviewSummaryResponseDto;
import org.example.mentoring.review.infrastructure.repository.ReviewRepository;
import org.example.mentoring.global.security.MentoringUserDetails;
import org.example.mentoring.user.domain.User;
import org.example.mentoring.user.infrastructure.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReviewCreateResponseDto createReview(ReviewCreateRequestDto reviewCreateRequestDto, MentoringUserDetails menteeDetails) {
        User reviewer = findUser(menteeDetails);
        Reservation reservation = findReservation(reviewCreateRequestDto.reservationId());

        validateCreation(reservation, reviewer);

        Listing listing = reservation.getListing();
        listing.applyRating(reviewCreateRequestDto.rating());

        Review review = Review.builder()
                .reservation(reservation)
                .listing(listing)
                .reviewer(reviewer)
                .rating(reviewCreateRequestDto.rating().byteValue())
                .content(reviewCreateRequestDto.content())
                .build();

        return ReviewCreateResponseDto.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponseDto getReview(Long reviewId) {
        Review review = reviewRepository.findDetailById(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewDetailResponseDto.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewSummaryResponseDto> getReviews(ReviewSearchRequestDto requestDto) {
        int page = requestDto.page() == null ? 0 : requestDto.page();
        int size = requestDto.size() == null ? 10 : requestDto.size();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return reviewRepository.findByListingId(requestDto.listingId(), pageable)
                .map(ReviewSummaryResponseDto::from);
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private User findUser(MentoringUserDetails menteeDetails) {
        return userRepository
                .findById(menteeDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
    private void validateCreation(Reservation reservation, User reviewer) {
        if(!reservation.getMentee().getId().equals(reviewer.getId()))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);

        if(!reservation.getStatus().equals(ReservationStatus.COMPLETED))
            throw new BusinessException(ErrorCode.RESERVATION_NOT_COMPLETED);

        if(reviewRepository.existsByReservationId(reservation.getId()))
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
