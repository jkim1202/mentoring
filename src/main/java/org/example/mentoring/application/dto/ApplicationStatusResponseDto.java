package org.example.mentoring.application.dto;

import org.example.mentoring.application.entity.ApplicationStatus;

public record ApplicationStatusResponseDto(Long id,ApplicationStatus status){
}
