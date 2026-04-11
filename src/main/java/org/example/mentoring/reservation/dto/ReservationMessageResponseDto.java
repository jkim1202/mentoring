package org.example.mentoring.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.reservation.entity.ReservationMessage;

import java.time.LocalDateTime;

@Schema(description = "예약 메시지 응답 항목")
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
