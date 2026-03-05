package org.example.mentoring.listing.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.mentoring.listing.dto.ListingSearchRequestDto;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.QListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public class ListingRepositoryCustomImpl implements ListingRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public ListingRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }


    @Override
    public Page<Listing> search(ListingSearchRequestDto req, Pageable pageable) {
        List<Listing> listings = jpaQueryFactory
                .select(QListing.listing)
                .from(QListing.listing)
                .where(
                        topicEq(req),
                        placeTypeEq(req),
                        priceBetween(req)
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(QListing.listing.count())
                .from(QListing.listing)
                .where(
                        topicEq(req),
                        placeTypeEq(req),
                        priceBetween(req)
                );
        return PageableExecutionUtils.getPage(listings, pageable, countQuery::fetchOne);
    }

    private BooleanExpression topicEq(ListingSearchRequestDto req) {
        return StringUtils.hasText(req.topic()) ? QListing.listing.topic.containsIgnoreCase(req.topic()) : null;
    }

    private BooleanExpression placeTypeEq(ListingSearchRequestDto req) {
        return req.placeType() != null ? QListing.listing.placeType.eq(req.placeType()) : null;
    }

    private BooleanExpression priceBetween(ListingSearchRequestDto req) {
        if (req.minPrice() == null && req.maxPrice() == null) return null;
        if (req.maxPrice() == null) return QListing.listing.price.goe(req.minPrice());
        if (req.minPrice() == null) return QListing.listing.price.loe(req.maxPrice());
        return QListing.listing.price.between(req.minPrice(), req.maxPrice());
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        return pageable.getSort().stream()
                .map(this::toOrderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order sortOrder) {
        com.querydsl.core.types.Order direction =
                sortOrder.isAscending()
                        ? com.querydsl.core.types.Order.ASC
                        : com.querydsl.core.types.Order.DESC;

        return switch (sortOrder.getProperty()) {
            case "price" -> new OrderSpecifier<>(direction, QListing.listing.price);
            case "createdAt" -> new OrderSpecifier<>(direction, QListing.listing.createdAt);
            case "avgRating" -> new OrderSpecifier<>(direction, QListing.listing.avgRating);
            case "reviewCount" -> new OrderSpecifier<>(direction, QListing.listing.reviewCount);
            default -> new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QListing.listing.createdAt);
        };
    }
}
