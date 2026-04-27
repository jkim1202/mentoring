package org.example.mentoring.like.controller;

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
public class LikeController {
    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/listings/{listingId}/like")
    public ResponseEntity<LikeDetailsDto> toggleLike(@PathVariable Long listingId, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        LikeDetailsDto likeDetailsDto = likeService.toggleLike(listingId, userDetails);

        return ResponseEntity.ok(likeDetailsDto);
    }

    @GetMapping("/users/me/likes")
    public ResponseEntity<Page<LikeSummaryResponseDto>> getMyLikes(
            @Valid @ModelAttribute LikeSearchRequestDto requestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.ok(likeService.getMyLikes(requestDto, userDetails));
    }
}
