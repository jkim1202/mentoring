package org.example.mentoring.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.auth.dto.*;
import org.example.mentoring.auth.service.AuthService;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {
        RegisterRequestDto req = new RegisterRequestDto("test@test.com", "password123");
        RegisterResponseDto res = new RegisterResponseDto("test@test.com", UserStatus.ACTIVE);
        given(authService.register(req)).willReturn(res);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"));
    }
    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        LoginRequestDto req = new LoginRequestDto("test@test.com", "password123");
        LoginResponseDto res = new LoginResponseDto("access-token", "refresh-token");

        given(authService.login(req)).willReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("로그인 실패 - AUTH_LOGIN_FAILED")
    void login_fail() throws Exception {
        LoginRequestDto req = new LoginRequestDto("test@test.com", "wrong-password");

        given(authService.login(any(LoginRequestDto.class)))
                .willThrow(new BusinessException(ErrorCode.AUTH_LOGIN_FAILED));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_001"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_success() throws Exception {
        RefreshRequestDto req = new RefreshRequestDto("refresh-token");
        RefreshResponseDto res = new RefreshResponseDto("new-access", "new-refresh");

        given(authService.refreshToken(any(RefreshRequestDto.class))).willReturn(res);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("요청 검증 실패 - 이메일 형식 오류")
    void login_validation_fail() throws Exception {
        LoginRequestDto req = new LoginRequestDto("not-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }
}
