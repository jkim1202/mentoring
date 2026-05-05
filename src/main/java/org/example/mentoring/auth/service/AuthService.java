package org.example.mentoring.auth.service;

import org.example.mentoring.auth.dto.LoginRequestDto;
import org.example.mentoring.auth.dto.LoginResponseDto;
import org.example.mentoring.auth.dto.RefreshRequestDto;
import org.example.mentoring.auth.dto.RefreshResponseDto;
import org.example.mentoring.auth.dto.RegisterRequestDto;
import org.example.mentoring.auth.dto.RegisterResponseDto;
import org.example.mentoring.auth.entity.RefreshToken;
import org.example.mentoring.auth.repository.RefreshTokenRepository;
import org.example.mentoring.user.entity.Role;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.user.repository.UserRepository;
import org.example.mentoring.security.JwtTokenProvider;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.HexFormat;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto authRequestDto) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(authRequestDto.email()).isPresent()) {
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
        } catch (AccountStatusException e) {
            throw new BusinessException(ErrorCode.AUTH_STATUS_NOT_ACTIVE);
        }catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.email());
        if (!userDetails.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_STATUS_NOT_ACTIVE);
        }
        User user = userRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        saveOrRotateRefreshToken(user, refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public RefreshResponseDto refreshToken(RefreshRequestDto refreshRequestDto) {
        String refreshToken = refreshRequestDto.refreshToken();
        String email = jwtTokenProvider.getEmailFromRefreshToken(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtTokenProvider.validateRefreshToken(refreshToken, userDetails)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
        if (!savedToken.getUser().getEmail().equals(email) || savedToken.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
        if (!userDetails.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_STATUS_NOT_ACTIVE);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        savedToken.rotate(hash(newRefreshToken), toLocalDateTime(jwtTokenProvider.getRefreshTokenExpiration(newRefreshToken)));
        return new RefreshResponseDto(newAccessToken, newRefreshToken);
    }

    private void saveOrRotateRefreshToken(User user, String refreshToken) {
        String tokenHash = hash(refreshToken);
        LocalDateTime expiresAt = toLocalDateTime(jwtTokenProvider.getRefreshTokenExpiration(refreshToken));
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        savedToken -> savedToken.rotate(tokenHash, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, tokenHash, expiresAt))
                );
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is not available", e);
        }
    }
}
