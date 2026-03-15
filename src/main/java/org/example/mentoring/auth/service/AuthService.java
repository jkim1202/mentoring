package org.example.mentoring.auth.service;

import org.example.mentoring.auth.dto.*;
import org.example.mentoring.user.entity.Role;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.user.repository.UserRepository;
import org.example.mentoring.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto authRequestDto) {
        // 이메일 중복 확인
        if(userRepository.findByEmail(authRequestDto.email()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.USER);

        String encodedPassword = passwordEncoder.encode(authRequestDto.password());
        User user = User.builder()
                .email(authRequestDto.email())
                .passwordHash(encodedPassword)
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();
        userRepository.save(user);

        return new RegisterResponseDto(user.getEmail(), user.getStatus());
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.email(), loginRequestDto.password()));
        }catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.email());
        if(!userDetails.isEnabled()) throw new BusinessException(ErrorCode.AUTH_STATUS_NOT_ACTIVE);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public RefreshResponseDto refreshToken(RefreshRequestDto refreshRequestDto) {
        String refreshToken = refreshRequestDto.refreshToken();
        String email = jwtTokenProvider.getEmailFromRefreshToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if(!jwtTokenProvider.validateRefreshToken(refreshToken, userDetails))
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        return new RefreshResponseDto(newAccessToken, newRefreshToken);
    }
}
