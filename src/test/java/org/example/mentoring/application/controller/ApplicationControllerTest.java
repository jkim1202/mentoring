package org.example.mentoring.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApplicationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
public class ApplicationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("신청 성공")
    void create_application_success() throws Exception {
        ApplicationCreateRequestDto req = new ApplicationCreateRequestDto(
                1L,
                1L,
                "새 예약"
        );

        ApplicationCreateResponseDto res = new ApplicationCreateResponseDto(
                1L,
                ApplicationStatus.APPLIED,
                "새 예약"
        );

        given(applicationService.createApplication(any(ApplicationCreateRequestDto.class), any())).willReturn(res);

        mockMvc.perform(post("/api/applications")
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.message").value("새 예약"));
    }

    @Test
    @DisplayName("중복 신청 실패")
    void create_application_duplicate_fail() throws Exception {
        ApplicationCreateRequestDto req = new ApplicationCreateRequestDto(
                1L,
                1L,
                "새 예약"
        );

        given(applicationService.createApplication(any(ApplicationCreateRequestDto.class), any()))
                .willThrow(new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS));

        mockMvc.perform(post("/api/applications")
                        .with(authentication(authOf(20L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPLICATION_002"));
    }

    @Test
    @DisplayName("slot-listing 불일치 실패")
    void create_application_slot_not_belong_to_listing_fail() throws Exception {
        ApplicationCreateRequestDto req = new ApplicationCreateRequestDto(
                1L,
                1L,
                "새 예약"
        );

        given(applicationService.createApplication(any(ApplicationCreateRequestDto.class), any()))
                .willThrow(new BusinessException(ErrorCode.SLOT_NOT_BELONG_TO_LISTING));

        mockMvc.perform(post("/api/applications")
                        .with(authentication(authOf(30L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SLOT_002"));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails principal = new MentoringUserDetails(
                userId,
                "test@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
