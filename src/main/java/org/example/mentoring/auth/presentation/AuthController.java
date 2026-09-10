package org.example.mentoring.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.mentoring.auth.application.AuthService;
import org.example.mentoring.auth.presentation.dto.LoginRequestDto;
import org.example.mentoring.auth.presentation.dto.LoginResponseDto;
import org.example.mentoring.auth.presentation.dto.RefreshRequestDto;
import org.example.mentoring.auth.presentation.dto.RefreshResponseDto;
import org.example.mentoring.auth.presentation.dto.RegisterRequestDto;
import org.example.mentoring.auth.presentation.dto.RegisterResponseDto;
import org.example.mentoring.global.security.MentoringUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 토큰 재발급 API")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 새 계정을 생성한다.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "중복 이메일",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto authRequestDto) {
        RegisterResponseDto responseDto = authService.register(authRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 access/refresh 토큰을 발급한다.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto responseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "refresh token으로 새 access/refresh token을 발급한다.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RefreshResponseDto> refreshToken(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        RefreshResponseDto responseDto = authService.refreshToken(refreshRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "서버에 저장된 refresh token을 삭제한다. 기존 access token은 만료 전까지 유효하다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        authService.logout(userDetails);
        return ResponseEntity.noContent().build();
    }
}
