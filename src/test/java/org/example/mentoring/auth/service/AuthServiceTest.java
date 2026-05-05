package org.example.mentoring.auth.service;

import org.example.mentoring.auth.dto.RefreshRequestDto;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtTokenProvider;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("비활성 계정은 refresh token으로 토큰을 재발급 받을 수 없다")
    void refresh_token_inactive_user_fail() {
        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                authenticationManager,
                userDetailsService
        );
        String refreshToken = "refresh-token";
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L,
                "user@test.com",
                "password",
                UserStatus.SUSPENDED,
                List.of()
        );

        given(jwtTokenProvider.getEmailFromRefreshToken(refreshToken)).willReturn("user@test.com");
        given(userDetailsService.loadUserByUsername("user@test.com")).willReturn(userDetails);
        given(jwtTokenProvider.validateRefreshToken(refreshToken, userDetails)).willReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequestDto(refreshToken)))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_STATUS_NOT_ACTIVE)
                );
        verify(jwtTokenProvider, never()).generateAccessToken(userDetails);
        verify(jwtTokenProvider, never()).generateRefreshToken(userDetails);
    }
}
