package org.example.mentoring.scheduler;

import org.example.mentoring.expiration.ExpirationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationSchedulerTest {

    @Mock
    private ExpirationUseCase expirationUseCase;

    @InjectMocks
    private ReservationExpirationScheduler reservationExpirationScheduler;

    @Test
    @DisplayName("스케줄러는 예약 만료 처리를 호출한다")
    void schedule_calls_expire_pending_reservations() {
        reservationExpirationScheduler.expirePendingReservations();

        then(expirationUseCase).should().execute();
    }
}
