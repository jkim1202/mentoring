package org.example.mentoring.application.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.dto.ApplicationDetailResponseDto;
import org.example.mentoring.application.dto.ApplicationSearchRequestDto;
import org.example.mentoring.application.dto.ApplicationStatusResponseDto;
import org.example.mentoring.application.dto.ApplicationSummaryResponseDto;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Application", description = "신청 생성, 조회, 수락, 거절 API")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @Operation(summary = "신청 생성", description = "멘티가 특정 등록글의 슬롯에 신청을 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신청 생성 성공"),
            @ApiResponse(responseCode = "400", description = "중복 신청 또는 슬롯 정책 위반", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ApplicationCreateResponseDto> createApplication(
            @Valid @RequestBody ApplicationCreateRequestDto applicationCreateRequestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(applicationCreateRequestDto, userDetails));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "신청 수락", description = "멘토가 신청을 수락하고 예약을 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공"),
            @ApiResponse(responseCode = "400", description = "수락 불가 상태 또는 시작 시간 경과", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "멘토 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ApplicationStatusResponseDto> acceptApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, userDetails, ApplicationStatus.ACCEPTED));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "신청 거절", description = "멘토가 신청을 거절한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "403", description = "멘토 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ApplicationStatusResponseDto> rejectApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, userDetails, ApplicationStatus.REJECTED));
    }

    @GetMapping("/{id}")
    @Operation(summary = "신청 상세 조회", description = "신청 당사자 또는 멘토가 신청 상세를 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "신청 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ApplicationDetailResponseDto> getApplication(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(applicationService.getApplication(id, userDetails));
    }

    @GetMapping
    @Operation(summary = "신청 목록 조회", description = "멘토/멘티 관점, 상태 필터, 정렬, 페이지 조건으로 신청 목록을 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Page<ApplicationSummaryResponseDto>> getApplications(
            @Valid @ModelAttribute ApplicationSearchRequestDto applicationSearchRequestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.ok(applicationService.getApplications(applicationSearchRequestDto, userDetails));
    }
}
