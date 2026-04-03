package org.example.mentoring.integration;

import org.assertj.core.api.AssertionsForClassTypes;
import org.example.mentoring.TestcontainersConfiguration;
import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.listing.repository.SlotRepository;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.Role;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
public class ApplicationReservationIntegrationTest {
    private record ApplicationFixture(
            User mentor,
            User mentee,
            Listing listing,
            Slot slot,
            Application application,
            MentoringUserDetails mentorDetails,
            MentoringUserDetails menteeDetails
    ) {}

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ListingRepository listingRepository;
    @Autowired
    private ReservationService reservationService;

    private ApplicationFixture createApplicationFixture() {
        User mentor = userRepository.save(
                User.builder()
                        .email("mentor@test.com")
                        .passwordHash("hashed-password")
                        .nickname("멘토닉네임")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(Role.USER))
                        .build()
        );

        User mentee = userRepository.save(
                User.builder()
                        .email("mentee@test.com")
                        .passwordHash("hashed-password")
                        .nickname("멘티닉네임")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(Role.USER))
                        .build()
        );

        Listing listing = listingRepository.save(
                Listing.builder()
                        .title("Spring 멘토링")
                        .mentor(mentor)
                        .topic("백엔드")
                        .price(30000)
                        .placeType(PlaceType.ONLINE)
                        .description("Spring 멘토링 설명")
                        .build()
        );

        Slot slot = slotRepository.save(
                Slot.builder()
                        .listing(listing)
                        .startAt(LocalDateTime.of(2026, 3, 17, 10, 0))
                        .endAt(LocalDateTime.of(2026, 3, 17, 11, 0))
                        .status(SlotStatus.OPEN)
                        .build()
        );

        Application application = applicationRepository.save(
                Application.builder()
                        .listing(listing)
                        .slot(slot)
                        .mentee(mentee)
                        .message("신청합니다")
                        .status(ApplicationStatus.APPLIED)
                        .build()
        );

        MentoringUserDetails mentorDetails = new MentoringUserDetails(
                mentor.getId(),
                mentor.getEmail(),
                "hashed-password",
                UserStatus.ACTIVE,
                List.of()
        );

        MentoringUserDetails menteeDetails = new MentoringUserDetails(
                mentee.getId(),
                mentee.getEmail(),
                "hashed-password",
                UserStatus.ACTIVE,
                List.of()
        );

        return new ApplicationFixture(mentor, mentee, listing, slot, application, mentorDetails, menteeDetails);
    }

    // 신청 수락 시 예약 생성 -> 슬롯 BOOKED
    @Test
    void accepting_application_creates_reservation_and_books_slot() throws BusinessException {
        // given: mentor, mentee, listing, slot, application, mentorDetails
        ApplicationFixture fixture = createApplicationFixture();

        // when: 멘토가 신청 수락
        applicationService.updateApplicationStatus(
                fixture.application().getId(),
                fixture.mentorDetails(),
                ApplicationStatus.ACCEPTED
        );

        // then: 예약 생성 + 슬롯 BOOKED
        Application acceptedApplication = applicationRepository.findById(fixture.application().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        Slot bookedSlot = slotRepository.findById(fixture.slot().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        assertThat(acceptedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(bookedSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(reservationRepository.findByApplicationId(fixture.application().getId())).isPresent();
    }

    // 예약 취소 시 슬롯 OPEN 재활성화
    @Test
    void canceling_reservation_changes_status_to_canceled_and_reopens_slot() throws BusinessException {
        // given: mentor, mentee, listing, slot, application, mentorDetails
        ApplicationFixture fixture = createApplicationFixture();

        // when : 멘토가 예약 수락 -> 취소
        applicationService.updateApplicationStatus(
                fixture.application().getId(),
                fixture.mentorDetails(),
                ApplicationStatus.ACCEPTED
        );

        Reservation reservation = reservationRepository.findByApplicationId(fixture.application().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        reservationService.cancelReservation(
                reservation.getId(),
                fixture.mentorDetails()
        );

        // then : 슬롯 상태 open, 예약 상태 cancel
        Reservation canceledReservation = reservationRepository.findByApplicationId(fixture.application().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        Slot reopenedSlot = slotRepository.findById(canceledReservation.getSlot().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        assertThat(canceledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reopenedSlot.getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    void reapplying_to_reopened_slot_creates_new_reservation() throws BusinessException {
        // given: mentor, mentee, listing, slot, application, mentorDetails, reservation
        ApplicationFixture fixture = createApplicationFixture();
        applicationService.updateApplicationStatus(fixture.application().getId() ,fixture.mentorDetails(), ApplicationStatus.ACCEPTED);
        Reservation reservation = reservationRepository.findByApplicationId(fixture.application().getId()).orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // when: 예약 취소 -> 슬롯 재활성화
        reservationService.cancelReservation(reservation.getId(), fixture.mentorDetails());

        Reservation canceledReservation = reservationRepository.findByApplicationId(fixture.application().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        Slot reopenedSlot = slotRepository.findById(canceledReservation.getSlot().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        // then: 같은 재활성화 슬롯에 재신청 후 새 예약 생성
        ApplicationCreateResponseDto responseDto = applicationService.createApplication(new ApplicationCreateRequestDto(reopenedSlot.getListing().getId(), reopenedSlot.getId(), "개인사정으로 취소 후 재신청합니다."), fixture.menteeDetails());
        Application newApplication = applicationRepository.findById(responseDto.id()).orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        applicationService.updateApplicationStatus(newApplication.getId(), fixture.mentorDetails(), ApplicationStatus.ACCEPTED);
        Reservation newReservation = reservationRepository.findByApplicationId(newApplication.getId()).orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        Slot rebookedSlot = slotRepository.findById(reopenedSlot.getId()).orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        assertThat(newReservation.getSlot()).isEqualTo(reopenedSlot);
        assertThat(newApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(rebookedSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void creating_duplicate_active_reservation_for_same_slot_fails() throws BusinessException {
        // given: mentor, mentee, listing, slot, application, mentorDetails
        ApplicationFixture fixture = createApplicationFixture();

        User mentee2 = userRepository.save(
                User.builder()
                        .email("mentee2@test.com")
                        .passwordHash("hashed-password")
                        .nickname("멘티2닉네임")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(Role.USER))
                        .build()
        );
        MentoringUserDetails mentee2Details = new MentoringUserDetails(
                mentee2.getId(),
                mentee2.getEmail(),
                "hashed-password",
                UserStatus.ACTIVE,
                List.of()
        );
        ApplicationCreateResponseDto responseDto = applicationService.createApplication(new ApplicationCreateRequestDto(fixture.listing().getId(), fixture.slot().getId(), "신청합니다. 감사합니다."), mentee2Details);
        Application application2 = applicationRepository.findById(responseDto.id()).orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // when: 첫 번째 신청 수락 -> 예약 생성
        applicationService.updateApplicationStatus(fixture.application().getId(), fixture.mentorDetails(), ApplicationStatus.ACCEPTED);

        // then: 두 번째 신청 수락 실패 -> 새 예약 생성 안 됨
        assertThatThrownBy(() -> applicationService.updateApplicationStatus(application2.getId(), fixture.mentorDetails(), ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        AssertionsForClassTypes.assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));
        // 신청1에 대한 예약
        Reservation reservation = reservationRepository.findByApplicationId(fixture.application().getId()).orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservation.getSlot().getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(reservationRepository.findByApplicationId(application2.getId())).isEmpty();
    }
}
