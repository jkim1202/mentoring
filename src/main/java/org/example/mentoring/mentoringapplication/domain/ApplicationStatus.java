package org.example.mentoring.mentoringapplication.domain;

public enum ApplicationStatus {
    APPLIED, ACCEPTED, REJECTED, CANCELED;

    public boolean canChangeTo(ApplicationStatus newStatus) {
        return switch (this) {
            case APPLIED ->  newStatus == ACCEPTED
                    || newStatus == REJECTED
                    || newStatus == CANCELED;
            default -> false;
        };
    }
}
