package org.example.mentoring.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.user.entity.User;

import java.time.LocalDateTime;

@Schema(description = "신청 상세 응답")
public record ApplicationDetailResponseDto(
        Long applicationId,
        ApplicationStatus applicationStatus,
        String message,
        Long listingId,
        String listingTitle,
        String listingTopic,
        Integer listingPrice,
        PlaceType placeType,
        String placeDesc,
        Long slotId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long partnerUserId,
        String partnerNickname,
        LocalDateTime createdAt
) {
    public static ApplicationDetailResponseDto from(Application application, Long loginUserId) {
        User partner = application.getListing().getMentor().getId().equals(loginUserId)
                ? application.getMentee()
                : application.getListing().getMentor();

        return new ApplicationDetailResponseDto(
                application.getId(),
                application.getStatus(),
                application.getMessage(),
                application.getListing().getId(),
                application.getListing().getTitle(),
                application.getListing().getTopic(),
                application.getListing().getPrice(),
                application.getListing().getPlaceType(),
                application.getListing().getPlaceDesc(),
                application.getSlot().getId(),
                application.getSlot().getStartAt(),
                application.getSlot().getEndAt(),
                partner.getId(),
                partner.getNickname(),
                application.getCreatedAt()
        );
    }
}
