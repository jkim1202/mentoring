package org.example.mentoring.reservation.controller;

import jakarta.validation.Valid;
import org.example.mentoring.reservation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.dto.ReservationMessageResponseDto;
import org.example.mentoring.reservation.service.ReservationMessageService;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations/{reservationId}/messages")
public class ReservationMessageController {
    private final ReservationMessageService reservationMessageService;

    @Autowired
    public ReservationMessageController(ReservationMessageService reservationMessageService) {
        this.reservationMessageService = reservationMessageService;
    }

    @PostMapping
    public ResponseEntity<ReservationMessageCreateResponseDto> createMessage(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationMessageCreateRequestDto requestDto,
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMessageService.createMessage(reservationId, requestDto, userDetails));
    }

    @GetMapping
    public ResponseEntity<List<ReservationMessageResponseDto>> getMessages(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal MentoringUserDetails userDetails) {
        return ResponseEntity.ok(reservationMessageService.getMessages(reservationId, userDetails));
    }
}
