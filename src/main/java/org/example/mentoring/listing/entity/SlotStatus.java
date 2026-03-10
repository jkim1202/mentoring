package org.example.mentoring.listing.entity;

import org.example.mentoring.application.entity.ApplicationStatus;

public enum SlotStatus {
    OPEN, BOOKED;
    public boolean canChangeTo(SlotStatus newStatus) {
        return switch (this) {
            case OPEN ->  newStatus == BOOKED;
            default -> false;
        };
    }
}
