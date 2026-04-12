package org.example.mentoring.mentor.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "멘토 프로필 생성 요청")
public record MentorProfileCreateRequestDto(
        @Schema(description = "멘토 소개", example = "백엔드 취업과 Spring 실무 위주로 멘토링합니다.")
        String bio,
        @Schema(description = "경력 연수", example = "5")
        Integer careerYears,
        @Schema(description = "전공", example = "컴퓨터공학")
        @Size(max = 100)
        String major,
        @Schema(description = "현재 회사", example = "Example Corp")
        @Size(max = 120)
        String currentCompany,
        @Schema(description = "자격증/포트폴리오/수상내역 등을 담는 JSON 문자열", example = "{\"portfolio\":\"https://example.com\"}")
        String specsJson,
        @Schema(description = "활동 기준 지역", example = "판교")
        @Size(max = 255)
        String baseLocation
) {
}
