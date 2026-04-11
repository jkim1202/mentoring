package org.example.mentoring.reservation.controller;

import jakarta.validation.Valid;
import org.example.mentoring.reservation.dto.ReservationDetailResponseDto;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<ReservationSummaryResponseDto> markPaid(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.markPaid(id, userDetails));
    }

    @PatchMapping("/{id}/confirm-paid")
    public ResponseEntity<ReservationSummaryResponseDto> confirmPaid(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.confirmPaid(id, userDetails));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationSummaryResponseDto> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, userDetails));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReservationSummaryResponseDto> completeReservation(@PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.completeReservation(id, userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDetailResponseDto> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservation(id, userDetails));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationSummaryResponseDto>> getReservations(
            @Valid @ModelAttribute ReservationSearchRequestDto requestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservations(requestDto, userDetails));
    }
}
