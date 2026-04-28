package org.example.mentoring.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.mentoring.like.dto.LikeDetailsDto;
import org.example.mentoring.like.dto.LikeSearchRequestDto;
import org.example.mentoring.like.dto.LikeSummaryResponseDto;
import org.example.mentoring.like.service.LikeService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Like", description = "찜 토글 및 내 찜 목록 조회 API")
public class LikeController {
    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/listings/{listingId}/like")
    @Operation(summary = "찜 토글", description = "현재 로그인한 사용자가 특정 등록글의 찜 상태를 토글한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "403", description = "본인 게시글 찜 불가 또는 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "등록글 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<LikeDetailsDto> toggleLike(
            @PathVariable Long listingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        LikeDetailsDto likeDetailsDto = likeService.toggleLike(listingId, userDetails);

        return ResponseEntity.ok(likeDetailsDto);
    }

    @GetMapping("/users/me/likes")
    @Operation(summary = "내 찜 목록 조회", description = "현재 로그인한 사용자의 찜 목록을 최신 찜 순으로 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Page<LikeSummaryResponseDto>> getMyLikes(
            @Valid @ModelAttribute LikeSearchRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.ok(likeService.getMyLikes(requestDto, userDetails));
    }
}
