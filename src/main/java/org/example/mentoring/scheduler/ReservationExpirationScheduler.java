package org.example.mentoring.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservationExpirationScheduler {
    private final ReservationService reservationService;

    @Autowired
    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void expirePendingReservations() {
        try {
            int expiredCount = reservationService.expirePendingReservations();
            if (expiredCount > 0) {
                log.info("Expired pending reservations count={}", expiredCount);
            }
        } catch (BusinessException e) {
            log.error("Failed to expire pending reservations. errorCode={}", e.getErrorCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error while expiring pending reservations", e);
        }
    }
}
