package org.example.mentoring.like.dto;

import org.example.mentoring.like.entity.Like;

public record LikeDetailsDto(Long userId, Long listingId, boolean liked) {
    public static LikeDetailsDto from(Like like, boolean liked) {
        return new LikeDetailsDto(like.getUser().getId(), like.getListing().getId(), liked);
    }
}
