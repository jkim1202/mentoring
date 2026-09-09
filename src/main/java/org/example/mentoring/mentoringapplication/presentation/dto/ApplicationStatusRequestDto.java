package org.example.mentoring.mentoringapplication.presentation.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.mentoringapplication.domain.ApplicationStatus;

public record ApplicationStatusRequestDto(@NotNull Long id, @NotNull ApplicationStatus applicationStatus) {
}
