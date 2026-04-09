package org.example.mentoring.reservation.service;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.service.SlotService;
import org.example.mentoring.reservation.dto.ReservationDetailResponseDto;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.example.mentoring.reservation.type.ReservationSort;
import org.example.mentoring.reservation.type.ReservationView;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SlotService slotService;

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

        given(slotService.findSlotByIdForUpdate(100L)).willReturn(slot);
        given(reservationRepository.existsBySlotIdAndStatusIn(100L,
                List.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED)))
                .willReturn(false);

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
    @DisplayName("예약 취소 후 재예약 성공")
    void cancel_then_create_new_reservation_on_reopened_slot_success() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        LocalDateTime endAt = LocalDateTime.now().plusHours(3);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(endAt)
                .status(SlotStatus.BOOKED)
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
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));
        willAnswer(invocation -> {
            slot.reopen();
            return null;
        }).given(slotService).releaseSlot(slot);

        ReservationSummaryResponseDto result =
                reservationService.cancelReservation(10000L, userDetails);

        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(result.partnerUserId()).isEqualTo(2L);
        assertThat(result.partnerNickname()).isEqualTo(mentee.getNickname());
        assertThat(result.slotStatus()).isEqualTo(SlotStatus.OPEN);
        then(reservationRepository).should().save(reservation);

        Application newApplication = Application.builder()
                .id(2000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.ACCEPTED)
                .build();


        // 재예약
        given(slotService.findSlotByIdForUpdate(100L)).willReturn(slot);
        given(reservationRepository.existsBySlotIdAndStatusIn(100L,
                List.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED)))
                .willReturn(false);

        reservationService.createReservation(newApplication);

        then(reservationRepository).should(times(2)).save(reservationCaptor.capture());

        Reservation savedReservation = reservationCaptor.getValue();

        assertThat(savedReservation.getApplication()).isEqualTo(newApplication);
        assertThat(savedReservation.getListing()).isEqualTo(listing);
        assertThat(savedReservation.getSlot()).isEqualTo(slot);
        assertThat(savedReservation.getMentor()).isEqualTo(mentor);
        assertThat(savedReservation.getMentee()).isEqualTo(mentee);
        assertThat(savedReservation.getStartAt()).isEqualTo(slot.getStartAt());
        assertThat(savedReservation.getEndAt()).isEqualTo(slot.getEndAt());
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("확정된 예약은 멘티가 24시간 이내 취소할 수 없다")
    void mentee_cannot_cancel_confirmed_reservation_within_24_hours() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(24);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.CONFIRMED)
                .build();

        MentoringUserDetails menteeDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(10000L, menteeDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_CANCEL_DEADLINE_EXCEEDED));

        then(reservationRepository).should(never()).save(any(Reservation.class));
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    @DisplayName("확정된 예약은 멘토가 시작 24시간 이내에도 취소할 수 있다")
    void mentor_can_cancel_confirmed_reservation_within_24_hours() {
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
                .title("Spring 멘토링")
                .build();

        LocalDateTime startAt = LocalDateTime.now().plusHours(24);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.CONFIRMED)
                .build();

        MentoringUserDetails mentorDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));
        willAnswer(invocation -> {
            slot.reopen();
            return null;
        }).given(slotService).releaseSlot(slot);

        ReservationSummaryResponseDto result = reservationService.cancelReservation(10000L, mentorDetails);

        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(result.slotStatus()).isEqualTo(SlotStatus.OPEN);
        then(reservationRepository).should().save(reservation);
    }

    @Test
    @DisplayName("예약 전체 조회 성공")
    void get_reservations_success() {
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
                .title("Spring 멘토링")
                .mentor(mentor)
                .build();

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.CONFIRMED)
                .build();

        ReservationSearchRequestDto req = new ReservationSearchRequestDto(
                0,
                10,
                ReservationView.MENTEE,
                ReservationSort.LATEST,
                ReservationFilter.UPCOMING
        );

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L,
                "mentee@test.com",
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );

        Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );

        given(reservationRepository.searchByMenteeId(
                ReservationFilter.UPCOMING,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                2L
        )).willReturn(reservationPage);

        Page<ReservationSummaryResponseDto> result = reservationService.getReservations(req, userDetails);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).reservationId()).isEqualTo(10000L);
        assertThat(result.getContent().get(0).reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getContent().get(0).listingId()).isEqualTo(10L);
        assertThat(result.getContent().get(0).listingTitle()).isEqualTo("Spring 멘토링");
        assertThat(result.getContent().get(0).partnerUserId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).partnerNickname()).isEqualTo("멘토닉네임");
        assertThat(result.getContent().get(0).slotStatus()).isEqualTo(SlotStatus.BOOKED);

        then(reservationRepository).should().searchByMenteeId(
                ReservationFilter.UPCOMING,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                2L
        );
    }

    @Test
    @DisplayName("예약 상세 조회 성공")
    void get_reservation_success() {
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
                .title("Spring 멘토링")
                .topic("Spring Boot")
                .price(50000)
                .placeType(PlaceType.OFFLINE)
                .placeDesc("강남역")
                .description("설명")
                .build();

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.CONFIRMED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L,
                "mentee@test.com",
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );

        given(reservationRepository.findDetailById(10000L)).willReturn(Optional.of(reservation));

        ReservationDetailResponseDto result = reservationService.getReservation(10000L, userDetails);

        assertThat(result.reservationId()).isEqualTo(10000L);
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.listingId()).isEqualTo(10L);
        assertThat(result.listingTitle()).isEqualTo("Spring 멘토링");
        assertThat(result.listingTopic()).isEqualTo("Spring Boot");
        assertThat(result.listingPrice()).isEqualTo(50000);
        assertThat(result.placeType()).isEqualTo(PlaceType.OFFLINE);
        assertThat(result.placeDesc()).isEqualTo("강남역");
        assertThat(result.partnerUserId()).isEqualTo(1L);
        assertThat(result.partnerNickname()).isEqualTo("멘토닉네임");
        assertThat(result.slotStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    @DisplayName("예약 당사자가 아니면 상세 조회 실패")
    void get_reservation_forbidden_fail() {
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
                .title("Spring 멘토링")
                .topic("Spring Boot")
                .price(50000)
                .placeType(PlaceType.ONLINE)
                .description("설명")
                .build();

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.CONFIRMED)
                .build();

        MentoringUserDetails otherUser = new MentoringUserDetails(
                3L,
                "other@test.com",
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );

        given(reservationRepository.findDetailById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.getReservation(10000L, otherUser))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("active 예약이 이미 존재하면 중복 예약 생성 실패")
    void create_reservation_when_active_reservation_exists_fail() {
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

        given(slotService.findSlotByIdForUpdate(100L)).willReturn(slot);
        given(reservationRepository.existsBySlotIdAndStatusIn(100L,
                List.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED)))
                .willReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(application))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.OPEN);
        then(reservationRepository).should(never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("멘티가 아니면 입금 표시 실패")
    void mark_paid_forbidden_fail() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.markPaid(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("예약 입금 확인 성공")
    void confirm_paid_success() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        ReservationSummaryResponseDto result =
                reservationService.confirmPaid(10000L, userDetails);

        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.partnerUserId()).isEqualTo(2L);
        assertThat(result.partnerNickname()).isEqualTo(mentee.getNickname());
        assertThat(reservation.getMentorPaidConfirmedAt()).isNotNull();
        then(reservationRepository).should().save(reservation);
    }

    @Test
    @DisplayName("멘티 입금 표시는 예약 생성 1시간이 지나면 실패")
    void mark_paid_fails_when_payment_window_expired() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.markPaid(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_PAYMENT_EXPIRED));
    }

    @Test
    @DisplayName("멘티 입금 표시는 예약 시작 시간이 지나면 실패")
    void mark_paid_fails_when_reservation_already_started() {
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

        LocalDateTime startAt = LocalDateTime.now().minusMinutes(1);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.markPaid(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_START_AT_EXPIRED));
    }

    @Test
    @DisplayName("멘토 입금 확인은 예약 시작 시간이 지나면 실패")
    void confirm_paid_fails_when_reservation_already_started() {
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

        LocalDateTime startAt = LocalDateTime.now().minusMinutes(1);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmPaid(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_START_AT_EXPIRED));
    }

    @Test
    @DisplayName("멘토가 아니면 입금 확인 실패")
    void confirm_paid_forbidden_fail() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmPaid(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("예약 완료 잘못된 상태 전이 실패")
    void complete_reservation_invalid_transition_fail() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.completeReservation(10000L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_INVALID_STATUS_TRANSITION));
    }

    @Test
    @DisplayName("멘티 입금 표시 성공")
    void mark_paid_success() {
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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
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
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        ReservationSummaryResponseDto result = reservationService.markPaid(10000L, userDetails);

        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(result.partnerUserId()).isEqualTo(1L);
        assertThat(result.partnerNickname()).isEqualTo(mentor.getNickname());
        assertThat(reservation.getMenteePaidMarkedAt()).isNotNull();
        then(reservationRepository).should().save(reservation);
    }

    @Test
    @DisplayName("만료 대상 pending 예약은 취소되고 미래 슬롯은 OPEN으로 복귀한다")
    void expire_pending_reservations_reopens_future_slot() {
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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = now.plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(now.minusHours(2))
                .build();

        given(reservationRepository.findPendingReservationsToExpire(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(reservation));

        Slot expiredReservationSlot = reservationService.expirePendingReservationsAndReturnSlots().getFirst();

        assertThat(expiredReservationSlot.getId()).isEqualTo(slot.getId());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        then(reservationRepository).should().saveAll(List.of(reservation));
    }

    @Test
    @DisplayName("시작 시간이 지난 pending 예약은 취소되고 슬롯은 EXPIRED 처리된다")
    void expire_pending_reservations_expires_started_slot() {
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

        LocalDateTime startAt = LocalDateTime.now().minusMinutes(1);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        Reservation reservation = Reservation.builder()
                .id(10000L)
                .listing(listing)
                .slot(slot)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();

        given(reservationRepository.findPendingReservationsToExpire(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(reservation));

        Slot expiredReservationSlot = reservationService.expirePendingReservationsAndReturnSlots().getFirst();

        assertThat(expiredReservationSlot.getId()).isEqualTo(slot.getId());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        then(reservationRepository).should().saveAll(List.of(reservation));
    }

    @Test
    @DisplayName("만료 대상이 없으면 빈 목록을 저장한다")
    void expire_pending_reservations_handles_empty_target() {
        given(reservationRepository.findPendingReservationsToExpire(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        List<Slot> expiredReservationSlotList = reservationService.expirePendingReservationsAndReturnSlots();

        assertThat(expiredReservationSlotList).isEmpty();
        then(reservationRepository).should().saveAll(List.of());
    }
}
