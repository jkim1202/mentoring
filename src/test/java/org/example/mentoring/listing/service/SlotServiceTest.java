package org.example.mentoring.listing.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.slot.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.slot.repository.SlotRepository;
import org.example.mentoring.slot.service.SlotService;
import org.example.mentoring.slot.dto.SlotCreateRequestDto;
import org.example.mentoring.slot.dto.SlotUpdateRequestDto;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.example.mentoring.listing.repository.ListingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SlotService slotService;

    @Test
    @DisplayName("슬롯 생성 성공")
    void create_slot_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );

        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));
        given(slotRepository.save(any(Slot.class))).willAnswer(invocation -> {
            Slot slot = invocation.getArgument(0);
            return Slot.builder()
                    .id(100L)
                    .listing(slot.getListing())
                    .startAt(slot.getStartAt())
                    .endAt(slot.getEndAt())
                    .status(slot.getStatus())
                    .build();
        });

        var result = slotService.createSlot(10L, requestDto, userDetails);

        assertThat(result.slotId()).isEqualTo(100L);
        assertThat(result.listingId()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    @DisplayName("등록글 작성자가 아니면 슬롯 생성 실패")
    void create_slot_forbidden_when_not_listing_owner() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );

        MentoringUserDetails userDetails = new MentoringUserDetails(
                2L, "other@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> slotService.createSlot(10L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("슬롯 생성 실패 - 시작 시각이 종료 시각과 같거나 늦으면 실패")
    void create_slot_fail_when_invalid_time_range() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(startAt, startAt);
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        assertThatThrownBy(() -> slotService.createSlot(10L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_INVALID_TIME_RANGE));
    }

    @Test
    @DisplayName("슬롯 생성 실패 - 과거 시작 시각")
    void create_slot_fail_when_start_at_in_past() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(1)
        );
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(listingRepository.findById(10L)).willReturn(Optional.of(listing));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        assertThatThrownBy(() -> slotService.createSlot(10L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_START_AT_IN_PAST));
    }

    @Test
    @DisplayName("슬롯 목록 조회 성공")
    void get_slots_success() {
        Listing listing = Listing.builder()
                .id(10L)
                .build();

        Slot firstSlot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 4, 20, 14, 0))
                .endAt(LocalDateTime.of(2026, 4, 20, 15, 0))
                .status(SlotStatus.OPEN)
                .build();
        Slot secondSlot = Slot.builder()
                .id(101L)
                .listing(listing)
                .startAt(LocalDateTime.of(2026, 4, 21, 14, 0))
                .endAt(LocalDateTime.of(2026, 4, 21, 15, 0))
                .status(SlotStatus.OPEN)
                .build();

        given(listingRepository.existsById(10L)).willReturn(true);
        given(slotRepository.searchByListingId(any(), any(), any(), any()))
                .willReturn(new org.springframework.data.domain.PageImpl<>(List.of(firstSlot, secondSlot)));

        var result = slotService.getSlots(10L, null, null, 0, 20);

        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().get(0).slotId()).isEqualTo(100L);
        assertThat(result.getContent().get(1).slotId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("슬롯 수정 성공")
    void update_slot_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(SlotStatus.OPEN)
                .build();

        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1)
        );
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        var result = slotService.updateSlot(100L, requestDto, userDetails);

        assertThat(result.slotId()).isEqualTo(100L);
        assertThat(result.startAt()).isEqualTo(requestDto.startAt());
        assertThat(result.endAt()).isEqualTo(requestDto.endAt());
    }

    @Test
    @DisplayName("이미 예약된 슬롯은 수정 실패")
    void update_slot_fail_when_booked() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(SlotStatus.BOOKED)
                .build();

        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1)
        );
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        assertThatThrownBy(() -> slotService.updateSlot(100L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));
    }

    @Test
    @DisplayName("시작 시간이 지난 슬롯은 수정 실패")
    void update_slot_fail_when_expired() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .build();

        Slot slot = Slot.builder()
                .id(100L)
                .listing(listing)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(30))
                .status(SlotStatus.OPEN)
                .build();

        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L, "mentor@test.com", "pw", UserStatus.ACTIVE, List.of()
        );

        given(slotRepository.findById(100L)).willReturn(Optional.of(slot));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));

        assertThatThrownBy(() -> slotService.updateSlot(100L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_EXPIRED));
    }

    @Test
    @DisplayName("시작 시간이 지난 OPEN 슬롯은 신청 가능 검증 시 EXPIRED 처리된다")
    void validate_slot_available_for_application_fails_when_slot_started() {
        Slot slot = Slot.builder()
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(59))
                .status(SlotStatus.OPEN)
                .build();

        assertThatThrownBy(() -> slotService.validateSlotAvailableForApplication(slot))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_EXPIRED));

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.EXPIRED);
    }

    @Test
    @DisplayName("미래 BOOKED 슬롯은 신청 가능 검증 시 이미 예약된 슬롯으로 실패한다")
    void validate_slot_available_for_application_fails_when_slot_booked() {
        Slot slot = Slot.builder()
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(2))
                .status(SlotStatus.BOOKED)
                .build();

        assertThatThrownBy(() -> slotService.validateSlotAvailableForApplication(slot))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SLOT_ALREADY_BOOKED));
    }

    @Test
    @DisplayName("미래 BOOKED 슬롯을 release 하면 OPEN으로 복귀한다")
    void release_slot_reopens_future_booked_slot() {
        Slot slot = Slot.builder()
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(2))
                .status(SlotStatus.BOOKED)
                .build();

        slotService.releaseSlot(slot);

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    @DisplayName("시작 시간이 지난 BOOKED 슬롯을 release 하면 EXPIRED 처리된다")
    void release_slot_expires_started_booked_slot() {
        Slot slot = Slot.builder()
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(59))
                .status(SlotStatus.BOOKED)
                .build();

        slotService.releaseSlot(slot);

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.EXPIRED);
    }
}
