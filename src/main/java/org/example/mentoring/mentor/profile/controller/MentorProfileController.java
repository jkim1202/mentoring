package org.example.mentoring.mentor.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.mentoring.mentor.profile.dto.MentorProfileCreateRequestDto;
import org.example.mentoring.mentor.profile.dto.MentorProfileResponseDto;
import org.example.mentoring.mentor.profile.service.MentorProfileService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@Tag(name = "MentorProfile", description = "멘토 프로필 생성 및 조회 API")
public class MentorProfileController {
    private final MentorProfileService mentorProfileService;

    public MentorProfileController(MentorProfileService mentorProfileService) {
        this.mentorProfileService = mentorProfileService;
    }

    @PostMapping("/me/profile")
    @Operation(summary = "멘토 프로필 생성", description = "로그인한 사용자의 멘토 프로필을 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "멘토 프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이미 멘토 프로필 존재", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<MentorProfileResponseDto> createProfile(
            @Valid @RequestBody MentorProfileCreateRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        MentorProfileResponseDto result = mentorProfileService.createProfile(userDetails.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{mentorUserId}/profile")
    @Operation(summary = "멘토 프로필 조회", description = "멘토 사용자 ID 기준으로 멘토 프로필을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멘토 프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "멘토 프로필 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<MentorProfileResponseDto> getProfile(@PathVariable Long mentorUserId) {
        return ResponseEntity.ok(mentorProfileService.getProfile(mentorUserId));
    }
}
