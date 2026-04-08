package org.example.mentoring.application.service;

import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.dto.ApplicationDetailResponseDto;
import org.example.mentoring.application.dto.ApplicationSearchRequestDto;
import org.example.mentoring.application.dto.ApplicationStatusResponseDto;
import org.example.mentoring.application.dto.ApplicationSummaryResponseDto;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.application.type.ApplicationSort;
import org.example.mentoring.application.type.ApplicationView;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
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
import static org.mockito.BDDMockito.*;

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
        given(applicationRepository.existsByMenteeIdAndSlotIdAndStatus(
                2L,
                100L,
                ApplicationStatus.APPLIED
        ))
                .willReturn(false);
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
        given(applicationRepository.existsByMenteeIdAndSlotIdAndStatus(
                2L,
                100L,
                ApplicationStatus.APPLIED
        ))
                .willReturn(true);

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

        LocalDateTime startAt = LocalDateTime.now().plusHours(2);
        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .slot(slot)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));
        given(userRepository.existsById(1L)).willReturn(true);

        ApplicationStatusResponseDto result =
                applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED);

        assertThat(result.status()).isEqualTo(ApplicationStatus.ACCEPTED);
        then(reservationService).should().createReservation(application);
    }

    @Test
    @DisplayName("시작 시간이 지난 신청은 수락 실패")
    void update_application_status_accept_expired_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
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
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .slot(slot)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));
        given(userRepository.existsById(1L)).willReturn(true);

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APPLICATION_ACCEPT_EXPIRED));

        then(reservationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("동일 슬롯 동시 수락 상황이면 예약 생성 실패가 전파된다")
    void update_application_status_accept_same_slot_concurrent_fail() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
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
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(100L)
                .listing(listing)
                .slot(slot)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));
        given(userRepository.existsById(1L)).willReturn(true);
        willThrow(new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED))
                .given(reservationService)
                .createReservation(application);

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));

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
        given(userRepository.existsById(1L)).willReturn(true);

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

        given(userRepository.existsById(2L)).willReturn(true);
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

        given(userRepository.existsById(1L)).willReturn(true);
        given(applicationRepository.findById(100L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(100L, userDetails, ApplicationStatus.ACCEPTED))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APPLICATION_INVALID_STATUS_TRANSITION));
    }

    @Test
    @DisplayName("신청 상세 조회 성공")
    void get_application_success() {
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
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .message("신청 메시지")
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findDetailById(1000L)).willReturn(Optional.of(application));

        ApplicationDetailResponseDto result = applicationService.getApplication(1000L, userDetails);

        assertThat(result.applicationId()).isEqualTo(1000L);
        assertThat(result.applicationStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(result.message()).isEqualTo("신청 메시지");
        assertThat(result.listingId()).isEqualTo(10L);
        assertThat(result.listingTitle()).isEqualTo("Spring 멘토링");
        assertThat(result.slotId()).isEqualTo(100L);
        assertThat(result.partnerUserId()).isEqualTo(1L);
        assertThat(result.partnerNickname()).isEqualTo("멘토닉네임");
    }

    @Test
    @DisplayName("신청 상세 조회 권한 실패")
    void get_application_forbidden_fail() {
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
                .status(SlotStatus.OPEN)
                .build();

        Application application = Application.builder()
                .id(1000L)
                .listing(listing)
                .slot(slot)
                .mentee(mentee)
                .status(ApplicationStatus.APPLIED)
                .build();

        MentoringUserDetails otherUser = new MentoringUserDetails(
                3L, "other@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(applicationRepository.findDetailById(1000L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.getApplication(1000L, otherUser))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("신청 목록 조회 성공")
    void get_applications_success() {
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
                .status(ApplicationStatus.APPLIED)
                .build();

        ApplicationSearchRequestDto req = new ApplicationSearchRequestDto(
                0,
                10,
                ApplicationView.MENTEE,
                ApplicationSort.LATEST,
                ApplicationFilter.PENDING
        );

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "mentee@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        Page<Application> applicationPage = new PageImpl<>(
                List.of(application),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );

        given(applicationRepository.searchByMenteeId(
                ApplicationFilter.PENDING,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                2L
        )).willReturn(applicationPage);

        Page<ApplicationSummaryResponseDto> result = applicationService.getApplications(req, userDetails);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).applicationId()).isEqualTo(1000L);
        assertThat(result.getContent().get(0).applicationStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(result.getContent().get(0).listingTitle()).isEqualTo("Spring 멘토링");
        assertThat(result.getContent().get(0).partnerUserId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).partnerNickname()).isEqualTo("멘토닉네임");

        then(applicationRepository).should().searchByMenteeId(
                ApplicationFilter.PENDING,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                2L
        );
    }
}
