package org.example.mentoring.reservation.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.slot.domain.SlotStatus;
import org.example.mentoring.reservation.domain.Reservation;
import org.example.mentoring.reservation.domain.ReservationStatus;
import org.example.mentoring.user.domain.User;

import java.time.LocalDateTime;

@Schema(description = "예약 목록 응답 항목")
public record ReservationSummaryResponseDto(
        Long reservationId,
        ReservationStatus reservationStatus,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long listingId,
        String listingTitle,
        Long partnerUserId,
        String partnerNickname,
        SlotStatus slotStatus
        ) {
    public static ReservationSummaryResponseDto from(Reservation reservation, Long loginUserId, SlotStatus slotStatus) {
        User partner = reservation.getMentor().getId().equals(loginUserId)
                ? reservation.getMentee()
                : reservation.getMentor();

        return new ReservationSummaryResponseDto(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getListing().getId(),
                reservation.getListing().getTitle(),
                partner.getId(),
                partner.getNickname(),
                slotStatus
        );
    }
    public static ReservationSummaryResponseDto from(Reservation reservation, Long loginUserId) {
        User partner = reservation.getMentor().getId().equals(loginUserId)
                ? reservation.getMentee()
                : reservation.getMentor();

        return new ReservationSummaryResponseDto(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getListing().getId(),
                reservation.getListing().getTitle(),
                partner.getId(),
                partner.getNickname(),
                reservation.getSlot().getStatus()
        );
    }
}
