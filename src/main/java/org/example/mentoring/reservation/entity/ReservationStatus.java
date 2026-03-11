package org.example.mentoring.reservation.entity;

public enum ReservationStatus {
    PENDING_PAYMENT, CONFIRMED, CANCELED, COMPLETED;

    public boolean canChangeTo(ReservationStatus newStatus) {
        return switch (this) {
            case PENDING_PAYMENT ->  newStatus == CONFIRMED || newStatus == CANCELED;
            case CONFIRMED -> newStatus == COMPLETED || newStatus == CANCELED;
            default -> false;
        };
    }
}
