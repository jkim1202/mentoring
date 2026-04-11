package org.example.mentoring.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.expiration.ExpirationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservationExpirationScheduler {
    private final ExpirationUseCase expirationUseCase;

    public ReservationExpirationScheduler(ExpirationUseCase expirationUseCase) {
        this.expirationUseCase = expirationUseCase;
    }

    @Scheduled(fixedDelay = 60000)
    public void expirePendingReservations() {
        try {
            expirationUseCase.execute();
        } catch (BusinessException e) {
            log.error("Failed to expire pending reservations. errorCode={}", e.getErrorCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error while expiring pending reservations", e);
        }
    }
}
