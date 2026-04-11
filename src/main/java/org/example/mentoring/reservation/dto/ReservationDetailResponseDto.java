package org.example.mentoring.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.user.entity.User;

import java.time.LocalDateTime;

@Schema(description = "예약 상세 응답")
public record ReservationDetailResponseDto(
        Long reservationId,
        ReservationStatus reservationStatus,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long listingId,
        String listingTitle,
        String listingTopic,
        Integer listingPrice,
        PlaceType placeType,
        String placeDesc,
        Long partnerUserId,
        String partnerNickname,
        SlotStatus slotStatus
) {
    public static ReservationDetailResponseDto from(Reservation reservation, Long loginUserId) {
        User partner = reservation.getMentor().getId().equals(loginUserId)
                ? reservation.getMentee()
                : reservation.getMentor();

        return new ReservationDetailResponseDto(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getListing().getId(),
                reservation.getListing().getTitle(),
                reservation.getListing().getTopic(),
                reservation.getListing().getPrice(),
                reservation.getListing().getPlaceType(),
                reservation.getListing().getPlaceDesc(),
                partner.getId(),
                partner.getNickname(),
                reservation.getSlot().getStatus()
        );
    }
}
