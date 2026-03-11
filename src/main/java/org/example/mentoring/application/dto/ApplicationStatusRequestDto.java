package org.example.mentoring.application.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.application.entity.ApplicationStatus;

public record ApplicationStatusRequestDto(@NotNull Long id, @NotNull ApplicationStatus applicationStatus) {
}
