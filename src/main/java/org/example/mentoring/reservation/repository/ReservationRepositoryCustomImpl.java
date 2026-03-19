package org.example.mentoring.reservation.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.mentoring.reservation.entity.QReservation;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class ReservationRepositoryCustomImpl implements ReservationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public ReservationRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<Reservation> searchByMentorId(ReservationFilter filter, Pageable pageable, Long mentorId){
        List<Reservation> content = jpaQueryFactory
                .select(QReservation.reservation)
                .from(QReservation.reservation)
                .where(
                        mentorIdEq(mentorId),
                        statusEq(filter)
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(QReservation.reservation.count())
                .from(QReservation.reservation)
                .where(
                        mentorIdEq(mentorId),
                        statusEq(filter)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Reservation> searchByMenteeId(ReservationFilter filter, Pageable pageable, Long menteeId){
        List<Reservation> content = jpaQueryFactory
                .select(QReservation.reservation)
                .from(QReservation.reservation)
                .where(
                        menteeIdEq(menteeId),
                        statusEq(filter)
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(QReservation.reservation.count())
                .from(QReservation.reservation)
                .where(
                        menteeIdEq(menteeId),
                        statusEq(filter)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable){
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
            case "createdAt" -> new OrderSpecifier<>(direction, QReservation.reservation.createdAt);
            case "startAt" -> new OrderSpecifier<>(direction, QReservation.reservation.startAt);
            default -> new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, QReservation.reservation.createdAt);
        };
    }
    private BooleanExpression menteeIdEq(Long menteeId) {
        return QReservation.reservation.mentee.id.eq(menteeId);
    }
    private BooleanExpression mentorIdEq(Long mentorId) {
        return QReservation.reservation.mentor.id.eq(mentorId);
    }
    private BooleanExpression statusEq(ReservationFilter filter) {
        return switch (filter) {
            case PENDING -> QReservation.reservation.status.eq(ReservationStatus.PENDING_PAYMENT);
            case UPCOMING -> QReservation.reservation.status.eq(ReservationStatus.CONFIRMED);
            case COMPLETED -> QReservation.reservation.status.eq(ReservationStatus.COMPLETED);
        };
    }
}
