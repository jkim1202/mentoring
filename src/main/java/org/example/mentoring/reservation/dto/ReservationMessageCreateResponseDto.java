package org.example.mentoring.reservation.dto;

import org.example.mentoring.reservation.entity.ReservationMessage;

public record ReservationMessageCreateResponseDto(
        Long messageId,
        Long reservationId,
        Long senderUserId,
        String content
) {
    public static ReservationMessageCreateResponseDto from(ReservationMessage reservationMessage) {
        return new ReservationMessageCreateResponseDto(
                reservationMessage.getId(),
                reservationMessage.getReservation().getId(),
                reservationMessage.getSender().getId(),
                reservationMessage.getContent()
        );
    }
}
