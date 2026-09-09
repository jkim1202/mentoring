package org.example.mentoring.reservation.application;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.mentoringapplication.application.MentoringApplicationService;
import org.example.mentoring.slot.domain.Slot;
import org.example.mentoring.slot.domain.SlotStatus;
import org.example.mentoring.slot.application.SlotService;
import org.example.mentoring.reservation.application.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ExpirationUseCase {
    private final ReservationService reservationService;
    private final MentoringApplicationService applicationService;
    private final SlotService slotService;

    public ExpirationUseCase(ReservationService reservationService, MentoringApplicationService applicationService, SlotService slotService) {
        this.reservationService = reservationService;
        this.applicationService = applicationService;
        this.slotService = slotService;
    }

    @Transactional
    public void execute(){
        List<Slot> slots = reservationService.expirePendingReservationsAndReturnSlots();

        slots.forEach(slotService::releaseSlot);

        List<Slot> expiredSlots =
                slots.stream()
                        .filter(s -> s.getStatus() == SlotStatus.EXPIRED)
                        .toList();
        log.info("Expired Slot count: {}", expiredSlots.size());

        applicationService.cancelAppliedApplicationsByExpiredSlots(expiredSlots);
    }
}
