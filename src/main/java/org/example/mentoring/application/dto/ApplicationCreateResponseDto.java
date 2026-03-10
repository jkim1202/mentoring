package org.example.mentoring.application.dto;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;

public record ApplicationCreateResponseDto(
        Long id,
        ApplicationStatus status,
        String message) {
    public static ApplicationCreateResponseDto from(Application application) {
        return new ApplicationCreateResponseDto(application.getId(), application.getStatus(), application.getMessage());
    }
}
