package org.example.mentoring.reservation.service;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Captor
    private ArgumentCaptor<Reservation> reservationCaptor;

    @Test
    @DisplayName("예약 생성 성공")
    void create_reservation_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User mentee = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.ACCEPTED)
                .build();

        reservationService.createReservation(application);

        then(reservationRepository).should().save(reservationCaptor.capture());

        Reservation savedReservation = reservationCaptor.getValue();

        assertThat(savedReservation.getApplication()).isEqualTo(application);
        assertThat(savedReservation.getListing()).isEqualTo(listing);
        assertThat(savedReservation.getSlot()).isEqualTo(slot);
        assertThat(savedReservation.getMentor()).isEqualTo(mentor);
        assertThat(savedReservation.getMentee()).isEqualTo(mentee);
        assertThat(savedReservation.getStartAt()).isEqualTo(slot.getStartAt());
        assertThat(savedReservation.getEndAt()).isEqualTo(slot.getEndAt());
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("예약 당사자가 아니면 상태 변경 실패")
    void update_reservation_status_forbidden_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User mentee = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.ACCEPTED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .application(application)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                3L, "otherUser@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateReservationStatus(10000L, ReservationStatus.CONFIRMED, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("예약 상태 CONFIRMED 변경 성공")
    void update_reservation_status_to_confirmed_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .nickname("멘토닉네임")
                .build();

        User mentee = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .nickname("멘티닉네임")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.ACCEPTED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .application(application)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        ReservationSummaryResponseDto result =
                reservationService.updateReservationStatus(10000L, ReservationStatus.CONFIRMED, userDetails);

        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.partnerUserId()).isEqualTo(2L);
        assertThat(result.partnerNickname()).isEqualTo(mentee.getNickname());
        then(reservationRepository).should().save(reservation);

    }

    @Test
    @DisplayName("예약 잘못된 상태 전이 실패")
    void update_reservation_status_invalid_transition_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User mentee = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.ACCEPTED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .application(application)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
                .status(ReservationStatus.CONFIRMED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateReservationStatus(10000L, ReservationStatus.PENDING_PAYMENT, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_INVALID_STATUS_TRANSITION));
    }
}
