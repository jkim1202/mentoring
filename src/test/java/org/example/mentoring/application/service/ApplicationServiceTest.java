package org.example.mentoring.application.service;

import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.dto.ApplicationStatusResponseDto;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.listing.repository.SlotRepository;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {
    @InjectMocks
    private ApplicationService applicationService;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private ReservationService reservationService;

    @Test
    @DisplayName("신청 생성 성공")
    void create_application_success() {
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
                .startAt(LocalDateTime.of(2026, 3, 14, 0, 0))
                .endAt(LocalDateTime.of(2026, 3, 14, 1, 30))
                .status(SlotStatus.OPEN)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );
        ApplicationCreateRequestDto req = new ApplicationCreateRequestDto(10L, 100L, "Application Create Success Test Message");

        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));
        given(applicationRepository.existsByMenteeIdAndSlotId(2L, 100L)).willReturn(false);
        given(applicationRepository.save(any(Application.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ApplicationCreateResponseDto result =
                applicationService.createApplication(req, userDetails);

        assertThat(result.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(result.message()).isEqualTo("Application Create Success Test Message");
    }

    @Test
    @DisplayName("멘티가 같은 슬롯에 중복 신청하면 실패")
    void create_application_duplicate_fail() {
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
                .startAt(LocalDateTime.of(2026, 3, 14, 0, 0))
                .endAt(LocalDateTime.of(2026, 3, 14, 1, 30))
                .status(SlotStatus.OPEN)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );
        ApplicationCreateRequestDto req = new ApplicationCreateRequestDto(10L, 100L, "Application Create Duplicate Fail Test Message");

        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));
        given(applicationRepository.existsByMenteeIdAndSlotId(2L, 100L)).willReturn(true);

        assertThatThrownBy(() -> applicationService.createApplication(req, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APPLICATION_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("슬롯이 해당 등록글에 속하지 않으면 신청 생성 실패")
    void create_application_slot_not_belong_to_listing_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User mentee = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .build();

        Listing requestListing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Listing actualSlotListing = Listing.builder()
                .id(20L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(actualSlotListing) // 요청 listing(10L)이 아니라 다른 listing(20L)에 속함
                .startAt(LocalDateTime.of(2026, 3, 14, 0, 0))
                .endAt(LocalDateTime.of(2026, 3, 14, 1, 0))
                .status(SlotStatus.OPEN)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        ApplicationCreateRequestDto req =
                new ApplicationCreateRequestDto(10L, 100L, "신청 메시지");

        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(listingRepository.findById(10L)).willReturn(Optional.of(requestListing));
        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));

        assertThatThrownBy(() -> applicationService.createApplication(req, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_NOT_BELONG_TO_LISTING));
    }

    @Test
    @DisplayName("신청 수락 성공 시 예약 생성 호출")
    void update_application_status_to_accepted_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        ApplicationStatusResponseDto result =
                applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED);

        assertThat(result.status()).isEqualTo(ApplicationStatus.ACCEPTED);
        then(reservationService).should().createReservation(application);
    }

    @Test
    @DisplayName("신청 거절 성공")
    void update_application_status_to_rejected_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        ApplicationStatusResponseDto result =
                applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.REJECTED);

        assertThat(result.status()).isEqualTo(ApplicationStatus.REJECTED);
        then(reservationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 BOOKED 슬롯이면 실패")
    void create_application_booked_slot_fail() {
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
                .startAt(LocalDateTime.of(2026, 3, 14, 0, 0))
                .endAt(LocalDateTime.of(2026, 3, 14, 1, 0))
                .status(SlotStatus.BOOKED) // 이미 예약된 슬롯
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        ApplicationCreateRequestDto req =
                new ApplicationCreateRequestDto(10L, 100L, "신청 메시지");

        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));

        assertThatThrownBy(() -> applicationService.createApplication(req, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));
    }

    @Test
    @DisplayName("멘토 본인이 아닌 사용자가 상태 변경 시 실패")
    void update_application_status_forbidden_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("mentee@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APPLICATION_NOT_BELONG_TO_MENTOR));
    }

    @Test
    @DisplayName("APPLIED가 아닌 상태에서 잘못된 전이 시 실패")
    void update_application_status_invalid_transition_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .status(ApplicationStatus.REJECTED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));
        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APPLICATION_INVALID_STATUS_TRANSITION));
    }
}
