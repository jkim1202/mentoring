package org.example.mentoring.listing.entity;

public enum ListingStatus {
    ACTIVE, INACTIVE, DELETED;

    public boolean canChangeTo(ListingStatus newStatus) {
        return switch (this) {
            case ACTIVE ->  newStatus == INACTIVE || newStatus == DELETED;
            case INACTIVE -> newStatus == ACTIVE || newStatus == DELETED;
            default -> false;
        };
    }
}
