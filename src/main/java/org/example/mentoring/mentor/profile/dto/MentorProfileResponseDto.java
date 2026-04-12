package org.example.mentoring.mentor.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.mentor.profile.entity.MentorProfile;

@Schema(description = "멘토 프로필 응답")
public record MentorProfileResponseDto(
        @Schema(description = "멘토 사용자 ID", example = "1")
        Long mentorUserId,
        @Schema(description = "멘토 닉네임", example = "백엔드멘토")
        String nickname,
        @Schema(description = "멘토 소개")
        String bio,
        @Schema(description = "경력 연수", example = "5")
        Integer careerYears,
        @Schema(description = "전공", example = "컴퓨터공학")
        String major,
        @Schema(description = "현재 회사", example = "Example Corp")
        String currentCompany,
        @Schema(description = "자격증/포트폴리오/수상내역 등을 담는 JSON 문자열")
        String specsJson,
        @Schema(description = "활동 기준 지역", example = "판교")
        String baseLocation,
        @Schema(description = "인증 여부", example = "false")
        boolean verified
) {
    public static MentorProfileResponseDto from(MentorProfile mentorProfile) {
        return new MentorProfileResponseDto(
                mentorProfile.getUserId(),
                mentorProfile.getUser().getNickname(),
                mentorProfile.getBio(),
                mentorProfile.getCareerYears(),
                mentorProfile.getMajor(),
                mentorProfile.getCurrentCompany(),
                mentorProfile.getSpecsJson(),
                mentorProfile.getBaseLocation(),
                mentorProfile.isVerifiedFlag()
        );
    }
}
