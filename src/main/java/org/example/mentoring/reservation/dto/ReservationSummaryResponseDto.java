package org.example.mentoring.reservation.dto;

import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.user.entity.User;

import java.time.LocalDateTime;

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
