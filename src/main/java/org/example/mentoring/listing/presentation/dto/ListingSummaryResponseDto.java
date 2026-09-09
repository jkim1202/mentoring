package org.example.mentoring.listing.presentation.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

@QueryProjection
public record ListingSummaryResponseDto (
    Long id,
    String title,
    String topic,
    Integer price,
    BigDecimal avgRating,
    Integer reviewCount,
    boolean liked
){}
