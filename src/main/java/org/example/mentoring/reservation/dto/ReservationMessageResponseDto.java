package org.example.mentoring.reservation.dto;

import org.example.mentoring.reservation.entity.ReservationMessage;

import java.time.LocalDateTime;

public record ReservationMessageResponseDto(
        Long messageId,
        Long senderUserId,
        String senderNickname,
        String content,
        LocalDateTime createdAt
) {
    public static ReservationMessageResponseDto from(ReservationMessage reservationMessage) {
        return new ReservationMessageResponseDto(
                reservationMessage.getId(),
                reservationMessage.getSender().getId(),
                reservationMessage.getSender().getNickname(),
                reservationMessage.getContent(),
                reservationMessage.getCreatedAt()
        );
    }
}
