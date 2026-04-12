package org.example.mentoring.mentor.profile.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.mentor.profile.dto.MentorProfileCreateRequestDto;
import org.example.mentoring.mentor.profile.dto.MentorProfileResponseDto;
import org.example.mentoring.mentor.profile.entity.MentorProfile;
import org.example.mentoring.mentor.profile.repository.MentorProfileRepository;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MentorProfileServiceTest {

    @InjectMocks
    private MentorProfileService mentorProfileService;

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("멘토 프로필 생성 성공")
    void create_profile_success() {
        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .nickname("멘토닉네임")
                .build();

        MentorProfileCreateRequestDto requestDto = new MentorProfileCreateRequestDto(
                "백엔드 멘토링을 진행합니다.",
                5,
                "컴퓨터공학",
                "Example Corp",
                "{\"portfolio\":\"https://example.com\"}",
                "판교"
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(mentorProfileRepository.existsById(1L)).willReturn(false);
        given(mentorProfileRepository.save(any(MentorProfile.class)))
                .willAnswer(invocation -> {
                    MentorProfile profile = invocation.getArgument(0);
                    return MentorProfile.builder()
                            .userId(user.getId())
                            .user(profile.getUser())
                            .bio(profile.getBio())
                            .careerYears(profile.getCareerYears())
                            .major(profile.getMajor())
                            .currentCompany(profile.getCurrentCompany())
                            .specsJson(profile.getSpecsJson())
                            .baseLocation(profile.getBaseLocation())
                            .verifiedFlag(profile.isVerifiedFlag())
                            .build();
                });

        MentorProfileResponseDto result = mentorProfileService.createProfile(1L, requestDto);

        assertThat(result.mentorUserId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("멘토닉네임");
        assertThat(result.bio()).isEqualTo("백엔드 멘토링을 진행합니다.");
        assertThat(result.careerYears()).isEqualTo(5);
        assertThat(result.verified()).isFalse();
    }

    @Test
    @DisplayName("이미 멘토 프로필이 있으면 생성 실패")
    void create_profile_fail_when_already_exists() {
        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .nickname("멘토닉네임")
                .build();

        MentorProfileCreateRequestDto requestDto = new MentorProfileCreateRequestDto(
                "소개",
                3,
                "컴퓨터공학",
                "Example Corp",
                null,
                "강남"
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(mentorProfileRepository.existsById(1L)).willReturn(true);

        assertThatThrownBy(() -> mentorProfileService.createProfile(1L, requestDto))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MENTOR_PROFILE_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("멘토 프로필 조회 성공")
    void get_profile_success() {
        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .nickname("멘토닉네임")
                .build();

        MentorProfile mentorProfile = MentorProfile.builder()
                .userId(1L)
                .user(user)
                .bio("백엔드 멘토링을 진행합니다.")
                .careerYears(5)
                .major("컴퓨터공학")
                .currentCompany("Example Corp")
                .specsJson("{\"portfolio\":\"https://example.com\"}")
                .baseLocation("판교")
                .build();

        given(mentorProfileRepository.findDetailByUserId(1L)).willReturn(Optional.of(mentorProfile));

        MentorProfileResponseDto result = mentorProfileService.getProfile(1L);

        assertThat(result.mentorUserId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("멘토닉네임");
        assertThat(result.currentCompany()).isEqualTo("Example Corp");
    }

    @Test
    @DisplayName("없는 멘토 프로필 조회 실패")
    void get_profile_fail_when_not_found() {
        given(mentorProfileRepository.findDetailByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mentorProfileService.getProfile(999L))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }
}
