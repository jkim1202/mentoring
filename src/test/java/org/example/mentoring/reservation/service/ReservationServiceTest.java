package org.example.mentoring.reservation.service;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.SlotRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SlotRepository slotRepository;

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

        given(slotRepository.findByIdForUpdate(100L)).willReturn(Optional.of(slot));
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

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
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
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(reservationRepository.findById(10000L)).willReturn(Optional.of(reservation));

        ReservationSummaryResponseDto result =
                reservationService.updateReservationStatus(10000L, ReservationStatus.CANCELED, userDetails);

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
        given(slotRepository.findByIdForUpdate(100L)).willReturn(Optional.of(slot));
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

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
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

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
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

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endAt(LocalDateTime.of(2026, 3, 15, 11, 0))
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

        given(slotRepository.findByIdForUpdate(100L)).willReturn(Optional.of(slot));
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
