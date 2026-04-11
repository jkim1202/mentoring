package org.example.mentoring.expiration;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.service.SlotService;
import org.example.mentoring.reservation.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ExpirationUseCase {
    private final ReservationService reservationService;
    private final ApplicationService applicationService;
    private final SlotService slotService;

    public ExpirationUseCase(ReservationService reservationService, ApplicationService applicationService, SlotService slotService) {
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
