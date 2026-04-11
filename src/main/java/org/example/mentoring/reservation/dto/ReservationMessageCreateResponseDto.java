package org.example.mentoring.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.reservation.entity.ReservationMessage;

@Schema(description = "예약 메시지 생성 응답")
public record ReservationMessageCreateResponseDto(
        @Schema(description = "메시지 ID", example = "1")
        Long messageId,
        @Schema(description = "예약 ID", example = "10")
        Long reservationId,
        @Schema(description = "발신자 사용자 ID", example = "2")
        Long senderUserId,
        @Schema(description = "메시지 내용")
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
