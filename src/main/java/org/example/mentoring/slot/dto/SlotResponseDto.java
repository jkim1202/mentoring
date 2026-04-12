package org.example.mentoring.slot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.slot.entity.Slot;

import java.time.LocalDateTime;

@Schema(description = "슬롯 응답")
public record SlotResponseDto(
        @Schema(description = "슬롯 ID", example = "1")
        Long slotId,
        @Schema(description = "등록글 ID", example = "10")
        Long listingId,
        @Schema(description = "시작 시각", example = "2026-04-20T14:00:00")
        LocalDateTime startAt,
        @Schema(description = "종료 시각", example = "2026-04-20T15:00:00")
        LocalDateTime endAt,
        @Schema(description = "슬롯 상태", example = "OPEN")
        SlotStatus status
) {
    public static SlotResponseDto from(Slot slot) {
        return new SlotResponseDto(
                slot.getId(),
                slot.getListing().getId(),
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getStatus()
        );
    }
}
