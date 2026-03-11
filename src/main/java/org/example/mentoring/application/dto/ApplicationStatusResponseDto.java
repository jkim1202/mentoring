package org.example.mentoring.application.dto;

import jakarta.validation.constraints.NotNull;
import org.example.mentoring.application.entity.ApplicationStatus;

public record ApplicationStatusResponseDto(Long id,ApplicationStatus applicationStatus){
}
