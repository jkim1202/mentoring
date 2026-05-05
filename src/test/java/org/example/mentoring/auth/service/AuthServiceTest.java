package org.example.mentoring.auth.service;

import org.example.mentoring.auth.dto.LoginRequestDto;
import org.example.mentoring.auth.dto.RefreshRequestDto;
import org.example.mentoring.auth.dto.RefreshResponseDto;
import org.example.mentoring.auth.entity.RefreshToken;
import org.example.mentoring.auth.repository.RefreshTokenRepository;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtTokenProvider;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("계정 상태 인증 실패는 AUTH_STATUS_NOT_ACTIVE로 응답한다")
    void login_inactive_user_fail() {
        AuthService authService = authService();
        LoginRequestDto request = new LoginRequestDto("user@test.com", "password");

        given(authenticationManager.authenticate(any()))
                .willThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_STATUS_NOT_ACTIVE)
                );
    }

    @Test
    @DisplayName("로그인 성공 시 refresh token을 저장한다")
    void login_success_saves_refresh_token() {
        AuthService authService = authService();
        User user = user(1L, "user@test.com");
        MentoringUserDetails userDetails = activeUserDetails();
        Date refreshExpiration = futureDate();

        given(userDetailsService.loadUserByUsername("user@test.com")).willReturn(userDetails);
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(userDetails)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(userDetails)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiration("refresh-token")).willReturn(refreshExpiration);
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.empty());

        authService.login(new LoginRequestDto("user@test.com", "password"));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getTokenHash()).isEqualTo(hash("refresh-token"));
    }

    @Test
    @DisplayName("refresh token 재발급 성공 시 저장된 토큰을 새 토큰으로 회전한다")
    void refresh_token_success_rotates_token() {
        AuthService authService = authService();
        User user = user(1L, "user@test.com");
        RefreshToken savedToken = RefreshToken.create(
                user,
                hash("old-refresh-token"),
                LocalDateTime.now().plusDays(1)
        );
        MentoringUserDetails userDetails = activeUserDetails();
        Date newRefreshExpiration = futureDate();

        given(jwtTokenProvider.getEmailFromRefreshToken("old-refresh-token")).willReturn("user@test.com");
        given(refreshTokenRepository.findByTokenHash(hash("old-refresh-token"))).willReturn(Optional.of(savedToken));
        given(userDetailsService.loadUserByUsername("user@test.com")).willReturn(userDetails);
        given(jwtTokenProvider.validateRefreshToken("old-refresh-token", userDetails)).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(userDetails)).willReturn("new-access-token");
        given(jwtTokenProvider.generateRefreshToken(userDetails)).willReturn("new-refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiration("new-refresh-token")).willReturn(newRefreshExpiration);

        RefreshResponseDto response = authService.refreshToken(new RefreshRequestDto("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(savedToken.getTokenHash()).isEqualTo(hash("new-refresh-token"));
    }

    @Test
    @DisplayName("저장된 현재 refresh token이 아니면 재발급할 수 없다")
    void refresh_token_reused_or_unknown_fail() {
        AuthService authService = authService();

        given(jwtTokenProvider.getEmailFromRefreshToken("old-refresh-token")).willReturn("user@test.com");
        given(refreshTokenRepository.findByTokenHash(hash("old-refresh-token"))).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequestDto("old-refresh-token")))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN)
                );
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("비활성 계정은 refresh token으로 토큰을 재발급 받을 수 없다")
    void refresh_token_inactive_user_fail() {
        AuthService authService = authService();
        String refreshToken = "refresh-token";
        User user = user(1L, "user@test.com");
        RefreshToken savedToken = RefreshToken.create(
                user,
                hash(refreshToken),
                LocalDateTime.now().plusDays(1)
        );
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L,
                "user@test.com",
                "password",
                UserStatus.SUSPENDED,
                List.of()
        );

        given(jwtTokenProvider.getEmailFromRefreshToken(refreshToken)).willReturn("user@test.com");
        given(refreshTokenRepository.findByTokenHash(hash(refreshToken))).willReturn(Optional.of(savedToken));
        given(userDetailsService.loadUserByUsername("user@test.com")).willReturn(userDetails);
        given(jwtTokenProvider.validateRefreshToken(refreshToken, userDetails)).willReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequestDto(refreshToken)))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_STATUS_NOT_ACTIVE)
                );
        verify(jwtTokenProvider, never()).generateAccessToken(userDetails);
        verify(jwtTokenProvider, never()).generateRefreshToken(userDetails);
    }

    private AuthService authService() {
        return new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                authenticationManager,
                userDetailsService
        );
    }

    private MentoringUserDetails activeUserDetails() {
        return new MentoringUserDetails(
                1L,
                "user@test.com",
                "password",
                UserStatus.ACTIVE,
                List.of()
        );
    }

    private User user(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .build();
    }

    private Date futureDate() {
        return new Date(System.currentTimeMillis() + 86_400_000L);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
