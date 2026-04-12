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
import org.example.mentoring.slot.dto.SlotResponseDto;
import org.example.mentoring.slot.dto.SlotUpdateRequestDto;
import org.example.mentoring.slot.service.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
@Tag(name = "Slot", description = "슬롯 수정 API")
public class SlotController {
    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PatchMapping("/{slotId}")
    @Operation(summary = "슬롯 수정", description = "특정 슬롯의 시간을 수정한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "슬롯 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "슬롯 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SlotResponseDto> updateSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody SlotUpdateRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails
    ) {
        return ResponseEntity.ok(slotService.updateSlot(slotId, requestDto, userDetails));
    }
}
