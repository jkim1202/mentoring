package org.example.mentoring.review.controller;

import jakarta.validation.Valid;
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
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewCreateResponseDto> createReview(
            @Valid @RequestBody ReviewCreateRequestDto reviewCreateRequestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(reviewCreateRequestDto, userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDetailResponseDto> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @GetMapping
    public ResponseEntity<Page<ReviewSummaryResponseDto>> getReviews(
            @Valid @ModelAttribute ReviewSearchRequestDto requestDto
    ) {
        return ResponseEntity.ok(reviewService.getReviews(requestDto));
    }
}
