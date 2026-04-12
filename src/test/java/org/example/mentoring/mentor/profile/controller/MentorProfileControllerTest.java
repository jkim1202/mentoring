package org.example.mentoring.mentor.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.mentor.profile.dto.MentorProfileCreateRequestDto;
import org.example.mentoring.mentor.profile.dto.MentorProfileResponseDto;
import org.example.mentoring.mentor.profile.service.MentorProfileService;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MentorProfileController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class MentorProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MentorProfileService mentorProfileService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("멘토 프로필 생성 성공")
    void create_profile_success() throws Exception {
        MentorProfileCreateRequestDto requestDto = new MentorProfileCreateRequestDto(
                "백엔드 멘토링을 진행합니다.",
                5,
                "컴퓨터공학",
                "Example Corp",
                "{\"portfolio\":\"https://example.com\"}",
                "판교"
        );
        MentorProfileResponseDto responseDto = new MentorProfileResponseDto(
                1L,
                "멘토닉네임",
                "백엔드 멘토링을 진행합니다.",
                5,
                "컴퓨터공학",
                "Example Corp",
                "{\"portfolio\":\"https://example.com\"}",
                "판교",
                false
        );

        given(mentorProfileService.createProfile(any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/mentors/me/profile")
                        .with(authentication(authOf(1L)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mentorUserId").value(1L))
                .andExpect(jsonPath("$.nickname").value("멘토닉네임"))
                .andExpect(jsonPath("$.careerYears").value(5));
    }

    @Test
    @DisplayName("이미 멘토 프로필이 있으면 생성 실패")
    void create_profile_fail_when_already_exists() throws Exception {
        MentorProfileCreateRequestDto requestDto = new MentorProfileCreateRequestDto(
                "소개",
                3,
                "컴퓨터공학",
                "Example Corp",
                null,
                "강남"
        );

        given(mentorProfileService.createProfile(any(), any()))
                .willThrow(new BusinessException(ErrorCode.MENTOR_PROFILE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/mentors/me/profile")
                        .with(authentication(authOf(1L)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MENTOR_PROFILE_002"));
    }

    @Test
    @DisplayName("멘토 프로필 조회 성공")
    void get_profile_success() throws Exception {
        MentorProfileResponseDto responseDto = new MentorProfileResponseDto(
                1L,
                "멘토닉네임",
                "백엔드 멘토링을 진행합니다.",
                5,
                "컴퓨터공학",
                "Example Corp",
                "{\"portfolio\":\"https://example.com\"}",
                "판교",
                false
        );

        given(mentorProfileService.getProfile(1L)).willReturn(responseDto);

        mockMvc.perform(get("/api/mentors/{mentorUserId}/profile", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentorUserId").value(1L))
                .andExpect(jsonPath("$.nickname").value("멘토닉네임"))
                .andExpect(jsonPath("$.currentCompany").value("Example Corp"));
    }

    @Test
    @DisplayName("없는 멘토 프로필 조회 실패")
    void get_profile_fail_when_not_found() throws Exception {
        given(mentorProfileService.getProfile(999L))
                .willThrow(new BusinessException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        mockMvc.perform(get("/api/mentors/{mentorUserId}/profile", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENTOR_PROFILE_001"));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails userDetails = new MentoringUserDetails(
                userId,
                "mentor@test.com",
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
