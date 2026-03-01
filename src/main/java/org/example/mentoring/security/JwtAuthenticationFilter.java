package org.example.mentoring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                email = jwtTokenProvider.getEmailFromAccessToken(jwtToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                boolean isTokenValid;
                try {
                     isTokenValid = jwtTokenProvider.validateAccessToken(jwtToken, userDetails);
                } catch (BusinessException e) {
                    request.setAttribute("errorCode", e.getErrorCode());
                    SecurityContextHolder.clearContext();
                    jwtAuthenticationEntryPoint.commence(
                            request,
                            response,
                            new InsufficientAuthenticationException("JWT auth failed")
                    );
                    return;
                }
                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            catch (Exception e) {
                ErrorCode ec = (e instanceof BusinessException be)
                        ? be.getErrorCode()
                        : ErrorCode.AUTH_INVALID_TOKEN;
                request.setAttribute("errorCode", ec);
                SecurityContextHolder.clearContext();
                jwtAuthenticationEntryPoint.commence(
                        request,
                        response,
                        new InsufficientAuthenticationException("JWT auth failed")
                );
            }
        } else {
            LOG.warn("JWT does not start with Bearer String");
        }

        filterChain.doFilter(request,response);
    }
}
