package org.example.mentoring.review.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.review.domain.Review;
import org.example.mentoring.review.dto.ReviewCreateRequestDto;
import org.example.mentoring.review.dto.ReviewCreateResponseDto;
import org.example.mentoring.review.repository.ReviewRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    @Test
    @DisplayName("리뷰 생성 성공")
    void create_review_success() {
        User mentor = createUser(1L, "mentor@test.com");
        User mentee = createUser(2L, "mentee@test.com");
        Listing listing = createListing(mentor);
        Reservation reservation = createReservation(10L, mentor, mentee, listing, ReservationStatus.COMPLETED);
        MentoringUserDetails menteeDetails = createUserDetails(mentee);
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(reservation.getId(), 5, "도움이 많이 됐습니다.");

        given(userRepository.findById(mentee.getId())).willReturn(Optional.of(mentee));
        given(reservationRepository.findById(reservation.getId())).willReturn(Optional.of(reservation));
        given(reviewRepository.existsByReservationId(reservation.getId())).willReturn(false);
        given(reviewRepository.save(org.mockito.ArgumentMatchers.any(Review.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ReviewCreateResponseDto response = reviewService.createReview(requestDto, menteeDetails);

        then(reviewRepository).should().save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();

        assertThat(savedReview.getReservation()).isEqualTo(reservation);
        assertThat(savedReview.getListing()).isEqualTo(listing);
        assertThat(savedReview.getReviewer()).isEqualTo(mentee);
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("도움이 많이 됐습니다.");
        assertThat(listing.getReviewCount()).isEqualTo(1);
        assertThat(listing.getAvgRating()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("도움이 많이 됐습니다.");
    }

    @Test
    @DisplayName("완료되지 않은 예약에는 리뷰를 생성할 수 없다")
    void create_review_fails_when_reservation_not_completed() {
        User mentor = createUser(1L, "mentor@test.com");
        User mentee = createUser(2L, "mentee@test.com");
        Listing listing = createListing(mentor);
        Reservation reservation = createReservation(10L, mentor, mentee, listing, ReservationStatus.CONFIRMED);
        MentoringUserDetails menteeDetails = createUserDetails(mentee);
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(reservation.getId(), 4, "좋았습니다.");

        given(userRepository.findById(mentee.getId())).willReturn(Optional.of(mentee));
        given(reservationRepository.findById(reservation.getId())).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reviewService.createReview(requestDto, menteeDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_NOT_COMPLETED));

        then(reviewRepository).should(never()).save(org.mockito.ArgumentMatchers.any(Review.class));
        assertThat(listing.getReviewCount()).isEqualTo(0);
        assertThat(listing.getAvgRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("같은 예약에는 중복 리뷰를 생성할 수 없다")
    void create_review_fails_when_review_already_exists() {
        User mentor = createUser(1L, "mentor@test.com");
        User mentee = createUser(2L, "mentee@test.com");
        Listing listing = createListing(mentor);
        Reservation reservation = createReservation(10L, mentor, mentee, listing, ReservationStatus.COMPLETED);
        MentoringUserDetails menteeDetails = createUserDetails(mentee);
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(reservation.getId(), 4, "좋았습니다.");

        given(userRepository.findById(mentee.getId())).willReturn(Optional.of(mentee));
        given(reservationRepository.findById(reservation.getId())).willReturn(Optional.of(reservation));
        given(reviewRepository.existsByReservationId(reservation.getId())).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(requestDto, menteeDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));

        then(reviewRepository).should(never()).save(org.mockito.ArgumentMatchers.any(Review.class));
        assertThat(listing.getReviewCount()).isEqualTo(0);
        assertThat(listing.getAvgRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .passwordHash("pw")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private MentoringUserDetails createUserDetails(User user) {
        return new MentoringUserDetails(
                user.getId(),
                user.getEmail(),
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );
    }

    private Listing createListing(User mentor) {
        return Listing.builder()
                .id(100L)
                .mentor(mentor)
                .title("Spring 멘토링")
                .topic("백엔드")
                .price(30000)
                .placeType(PlaceType.ONLINE)
                .description("설명")
                .build();
    }

    private Reservation createReservation(Long id, User mentor, User mentee, Listing listing, ReservationStatus status) {
        LocalDateTime startAt = LocalDateTime.of(2026, 4, 10, 10, 0);
        return Reservation.builder()
                .id(id)
                .listing(listing)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(status)
                .build();
    }
}
