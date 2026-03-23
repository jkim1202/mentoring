package org.example.mentoring.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.dto.ApplicationDetailResponseDto;
import org.example.mentoring.application.dto.ApplicationSearchRequestDto;
import org.example.mentoring.application.dto.ApplicationSummaryResponseDto;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.service.ApplicationService;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.application.type.ApplicationSort;
import org.example.mentoring.application.type.ApplicationView;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("신청 상세 조회 성공")
    void get_application_success() throws Exception {
        ApplicationDetailResponseDto res = new ApplicationDetailResponseDto(
                1L,
                ApplicationStatus.APPLIED,
                "신청 메시지",
                10L,
                "Spring 멘토링",
                "Spring Boot",
                50000,
                PlaceType.OFFLINE,
                "강남역",
                100L,
                LocalDateTime.of(2026, 3, 15, 10, 0),
                LocalDateTime.of(2026, 3, 15, 11, 0),
                2L,
                "멘티닉네임",
                LocalDateTime.of(2026, 3, 14, 9, 0)
        );

        given(applicationService.getApplication(any(), any())).willReturn(res);

        mockMvc.perform(get("/api/applications/{id}", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1))
                .andExpect(jsonPath("$.applicationStatus").value("APPLIED"))
                .andExpect(jsonPath("$.listingTitle").value("Spring 멘토링"))
                .andExpect(jsonPath("$.partnerNickname").value("멘티닉네임"));
    }

    @Test
    @DisplayName("신청 목록 조회 성공")
    void get_applications_success() throws Exception {
        List<ApplicationSummaryResponseDto> items = List.of(
                new ApplicationSummaryResponseDto(
                        1L,
                        ApplicationStatus.APPLIED,
                        10L,
                        "Spring 멘토링",
                        100L,
                        LocalDateTime.of(2026, 3, 15, 10, 0),
                        LocalDateTime.of(2026, 3, 15, 11, 0),
                        1L,
                        "멘토닉네임",
                        LocalDateTime.of(2026, 3, 14, 9, 0)
                )
        );

        given(applicationService.getApplications(any(), any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/applications")
                        .param("page", "0")
                        .param("size", "10")
                        .param("view", "MENTEE")
                        .param("sort", "LATEST")
                        .param("filter", "PENDING")
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicationId").value(1))
                .andExpect(jsonPath("$.content[0].applicationStatus").value("APPLIED"))
                .andExpect(jsonPath("$.content[0].listingTitle").value("Spring 멘토링"))
                .andExpect(jsonPath("$.content[0].partnerNickname").value("멘토닉네임"));

        then(applicationService).should().getApplications(any(ApplicationSearchRequestDto.class), any());
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
