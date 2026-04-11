package org.example.mentoring.application.controller;

import jakarta.validation.Valid;
import org.example.mentoring.application.dto.*;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationCreateResponseDto> createApplication(
            @Valid @RequestBody ApplicationCreateRequestDto applicationCreateRequestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(applicationCreateRequestDto, userDetails));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApplicationStatusResponseDto> acceptApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, userDetails, ApplicationStatus.ACCEPTED));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationStatusResponseDto> rejectApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, userDetails, ApplicationStatus.REJECTED));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDetailResponseDto> getApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.getApplication(id, userDetails));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationSummaryResponseDto>> getApplications(
            @Valid @ModelAttribute ApplicationSearchRequestDto applicationSearchRequestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.ok(applicationService.getApplications(applicationSearchRequestDto, userDetails));
    }
}
