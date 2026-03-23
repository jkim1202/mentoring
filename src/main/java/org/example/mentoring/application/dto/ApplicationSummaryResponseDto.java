package org.example.mentoring.application.dto;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.user.entity.User;

import java.time.LocalDateTime;

public record ApplicationSummaryResponseDto(
        Long applicationId,
        ApplicationStatus applicationStatus,
        Long listingId,
        String listingTitle,
        Long slotId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long partnerUserId,
        String partnerNickname,
        LocalDateTime createdAt
) {
    public static ApplicationSummaryResponseDto from(Application application, Long loginUserId) {
        User partner = application.getListing().getMentor().getId().equals(loginUserId)
                ? application.getMentee()
                : application.getListing().getMentor();

        return new ApplicationSummaryResponseDto(
                application.getId(),
                application.getStatus(),
                application.getListing().getId(),
                application.getListing().getTitle(),
                application.getSlot().getId(),
                application.getSlot().getStartAt(),
                application.getSlot().getEndAt(),
                partner.getId(),
                partner.getNickname(),
                application.getCreatedAt()
        );
    }
}
