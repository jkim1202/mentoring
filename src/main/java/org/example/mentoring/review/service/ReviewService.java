package org.example.mentoring.review.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.review.domain.Review;
import org.example.mentoring.review.dto.ReviewCreateRequestDto;
import org.example.mentoring.review.dto.ReviewCreateResponseDto;
import org.example.mentoring.review.repository.ReviewRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
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
                .rating(reviewCreateRequestDto.rating())
                .content(reviewCreateRequestDto.content())
                .build();

        return ReviewCreateResponseDto.from(reviewRepository.save(review));
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
