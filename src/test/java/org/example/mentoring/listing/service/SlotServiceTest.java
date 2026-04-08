package org.example.mentoring.listing.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private org.example.mentoring.listing.repository.SlotRepository slotRepository;

    @InjectMocks
    private SlotService slotService;

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
