package org.example.mentoring.listing.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.mentoring.listing.dto.ListingCreateRequestDto;
import org.example.mentoring.listing.dto.ListingResponseDto;
import org.example.mentoring.listing.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.dto.ListingStatusUpdateRequestDto;
import org.example.mentoring.listing.dto.ListingSummaryResponseDto;
import org.example.mentoring.listing.dto.ListingUpdateRequestDto;
import org.example.mentoring.listing.service.ListingService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listing", description = "멘토링 등록글 생성, 조회, 수정 API")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    @Operation(summary = "등록글 생성", description = "멘토링 등록글을 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ListingResponseDto> createListing(
            @Valid @RequestBody ListingCreateRequestDto request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.createListing(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "등록글 상세 조회", description = "등록글 단건 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "등록글 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ListingResponseDto> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    @GetMapping
    @Operation(summary = "등록글 목록 조회", description = "필터, 정렬, 페이지 조건으로 등록글 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Page<ListingSummaryResponseDto>> getListings(
            @ModelAttribute ListingSearchRequestDto request
    ) {
        Page<ListingSummaryResponseDto> result = listingService.getListings(request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "등록글 수정", description = "ACTIVE 상태의 등록글을 수정한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "수정 불가 상태 또는 입력값 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ListingResponseDto> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequestDto request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.updateListing(id, userDetails.getId(), request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "등록글 상태 변경", description = "등록글 상태를 ACTIVE, INACTIVE, DELETED로 변경한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 상태 전이", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ListingResponseDto> updateListingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingStatusUpdateRequestDto request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        ListingResponseDto result = listingService.updateStatus(id, userDetails.getId(), request);
        return ResponseEntity.ok(result);
    }

}
