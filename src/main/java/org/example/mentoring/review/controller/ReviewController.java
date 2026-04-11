package org.example.mentoring.review.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.mentoring.review.dto.ReviewCreateRequestDto;
import org.example.mentoring.review.dto.ReviewCreateResponseDto;
import org.example.mentoring.review.dto.ReviewDetailResponseDto;
import org.example.mentoring.review.dto.ReviewSearchRequestDto;
import org.example.mentoring.review.dto.ReviewSummaryResponseDto;
import org.example.mentoring.review.service.ReviewService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review", description = "리뷰 생성 및 조회 API")
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "리뷰 생성", description = "완료된 예약에 대해 리뷰를 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 생성 성공"),
            @ApiResponse(responseCode = "400", description = "완료되지 않은 예약 또는 중복 리뷰", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "작성 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReviewCreateResponseDto> createReview(
            @Valid @RequestBody ReviewCreateRequestDto reviewCreateRequestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(reviewCreateRequestDto, userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "리뷰 상세 조회", description = "리뷰 단건 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReviewDetailResponseDto> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @GetMapping
    @Operation(summary = "리뷰 목록 조회", description = "등록글 기준으로 리뷰 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Page<ReviewSummaryResponseDto>> getReviews(
            @Valid @ModelAttribute ReviewSearchRequestDto requestDto
    ) {
        return ResponseEntity.ok(reviewService.getReviews(requestDto));
    }
}
