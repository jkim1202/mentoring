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
import org.example.mentoring.reservation.dto.ReservationDetailResponseDto;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation", description = "예약 조회 및 상태 전이 API")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PatchMapping("/{id}/mark-paid")
    @Operation(summary = "입금 표시", description = "멘티가 예약에 대해 입금 완료를 표시한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입금 표시 성공"),
            @ApiResponse(responseCode = "400", description = "입금 가능 시간 만료 또는 상태 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationSummaryResponseDto> markPaid(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.markPaid(id, userDetails));
    }

    @PatchMapping("/{id}/confirm-paid")
    @Operation(summary = "입금 확인", description = "멘토가 입금 표시된 예약을 확인한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입금 확인 성공"),
            @ApiResponse(responseCode = "400", description = "예약 시작 시간 경과 또는 상태 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationSummaryResponseDto> confirmPaid(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.confirmPaid(id, userDetails));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "예약 취소", description = "예약 당사자가 예약을 취소한다. 취소 정책은 예약 상태와 시작 시간에 따라 달라진다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "취소 가능 시간 초과 또는 상태 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationSummaryResponseDto> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, userDetails));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "예약 완료", description = "멘토가 확정된 예약을 완료 처리한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "상태 오류", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationSummaryResponseDto> completeReservation(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.completeReservation(id, userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "예약 상세 조회", description = "예약 당사자가 예약 상세 정보를 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "예약 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ReservationDetailResponseDto> getReservation(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservation(id, userDetails));
    }

    @GetMapping
    @Operation(summary = "예약 목록 조회", description = "멘토/멘티 관점, 상태 필터, 정렬, 페이지 조건으로 예약 목록을 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Page<ReservationSummaryResponseDto>> getReservations(
            @Valid @ModelAttribute ReservationSearchRequestDto requestDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservations(requestDto, userDetails));
    }
}
