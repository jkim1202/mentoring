package org.example.mentoring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Test
    @DisplayName("비활성 계정의 access token 요청은 필터 체인을 진행하지 않는다")
    void inactive_user_access_token_fail() throws Exception {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtTokenProvider,
                userDetailsService,
                entryPoint
        );
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MentoringUserDetails userDetails = new MentoringUserDetails(
                1L,
                "user@test.com",
                "password",
                UserStatus.SUSPENDED,
                List.of()
        );

        request.addHeader("Authorization", "Bearer access-token");
        given(jwtTokenProvider.getEmailFromAccessToken("access-token")).willReturn("user@test.com");
        given(userDetailsService.loadUserByUsername("user@test.com")).willReturn(userDetails);
        given(jwtTokenProvider.validateAccessToken("access-token", userDetails)).willReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("AUTH_006");
        verify(filterChain, never()).doFilter(
                org.mockito.ArgumentMatchers.any(ServletRequest.class),
                org.mockito.ArgumentMatchers.any(ServletResponse.class)
        );
    }
}
