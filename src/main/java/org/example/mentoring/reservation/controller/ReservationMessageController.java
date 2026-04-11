package org.example.mentoring.reservation.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.mentoring.reservation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.dto.ReservationMessageResponseDto;
import org.example.mentoring.reservation.service.ReservationMessageService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations/{reservationId}/messages")
@Tag(name = "Reservation Message", description = "예약 단위 메시지 저장 및 조회 API")
public class ReservationMessageController {
    private final ReservationMessageService reservationMessageService;

    public ReservationMessageController(ReservationMessageService reservationMessageService) {
        this.reservationMessageService = reservationMessageService;
    }

    @PostMapping
    @Operation(summary = "예약 메시지 생성", description = "예약 당사자가 예약 스레드에 메시지를 전송한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "메시지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "메시지 전송 불가 상태", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationMessageCreateResponseDto> createMessage(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationMessageCreateRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMessageService.createMessage(reservationId, requestDto, userDetails));
    }

    @GetMapping
    @Operation(summary = "예약 메시지 목록 조회", description = "예약 당사자가 예약 메시지 목록을 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<List<ReservationMessageResponseDto>> getMessages(
            @PathVariable Long reservationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationMessageService.getMessages(reservationId, userDetails));
    }
}
