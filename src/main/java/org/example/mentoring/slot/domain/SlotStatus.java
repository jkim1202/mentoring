package org.example.mentoring.slot.domain;

public enum SlotStatus {
    OPEN, BOOKED, EXPIRED;
    public boolean canChangeTo(SlotStatus newStatus) {
        return switch (this) {
            case OPEN ->  newStatus == BOOKED || newStatus == EXPIRED;
            case BOOKED ->  newStatus == OPEN || newStatus == EXPIRED;
            case EXPIRED -> false;
        };
    }
}
