package org.example.mentoring.listing.dto;

import java.math.BigDecimal;

public record ListingSummaryResponseDto (
    Long id,
    String title,
    String topic,
    Integer price,
    BigDecimal avgRating,
    Integer reviewCount
){}
