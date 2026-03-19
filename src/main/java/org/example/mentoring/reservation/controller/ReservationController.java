package org.example.mentoring.reservation.controller;

import jakarta.validation.Valid;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationStatusUpdateRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationSummaryResponseDto> updateStatus(@Valid @RequestBody ReservationStatusUpdateRequestDto reservationStatusUpdateRequestDto, @PathVariable Long id, @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, reservationStatusUpdateRequestDto.status(), userDetails));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationSummaryResponseDto>> getReservations(
            @Valid @ModelAttribute ReservationSearchRequestDto requestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservations(requestDto, userDetails));
    }
}
