package org.example.mentoring.listing.entity;

import org.example.mentoring.application.entity.ApplicationStatus;

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
