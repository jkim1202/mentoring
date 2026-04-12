package org.example.mentoring.slot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.slot.dto.SlotCreateRequestDto;
import org.example.mentoring.slot.dto.SlotResponseDto;
import org.example.mentoring.slot.service.SlotService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings/{listingId}/slots")
@Tag(name = "ListingSlot", description = "등록글 하위 슬롯 생성 및 목록 API")
public class ListingSlotController {

    private final SlotService slotService;

    public ListingSlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @Operation(summary = "슬롯 생성", description = "특정 등록글 아래에 슬롯을 생성한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "슬롯 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SlotResponseDto> createSlot(
            @PathVariable Long listingId,
            @Valid @RequestBody SlotCreateRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createSlot(listingId, requestDto, userDetails));
    }

    @GetMapping
    @Operation(summary = "슬롯 목록 조회", description = "특정 등록글의 슬롯 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "슬롯 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "등록글 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Page<SlotResponseDto>> getSlots(
            @PathVariable Long listingId,
            @ModelAttribute SlotSearchRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                slotService.getSlots(listingId, requestDto.from(), requestDto.to(), requestDto.page(), requestDto.size())
        );
    }

    @Schema(description = "슬롯 목록 조회 요청")
    public record SlotSearchRequestDto(
            @Schema(description = "시작 조회 범위", example = "2026-04-20T00:00:00")
            java.time.LocalDateTime from,
            @Schema(description = "종료 조회 범위", example = "2026-04-30T23:59:59")
            java.time.LocalDateTime to,
            @Schema(description = "페이지 번호", example = "0")
            Integer page,
            @Schema(description = "페이지 크기", example = "20")
            Integer size
    ) {
    }
}
