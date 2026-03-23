package org.example.mentoring.application.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.entity.QApplication;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.listing.entity.QListing;
import org.example.mentoring.listing.entity.QSlot;
import org.example.mentoring.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class ApplicationRepositoryCustomImpl implements ApplicationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public ApplicationRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<Application> searchByMentorId(ApplicationFilter filter, Pageable pageable, Long mentorId) {
        return searchApplications(mentorIdEq(mentorId), filter, pageable);
    }

    @Override
    public Page<Application> searchByMenteeId(ApplicationFilter filter, Pageable pageable, Long menteeId) {
        return searchApplications(menteeIdEq(menteeId), filter, pageable);
    }

    private Page<Application> searchApplications(
            BooleanExpression participantCondition,
            ApplicationFilter filter,
            Pageable pageable
    ) {
        QApplication application = QApplication.application;
        QListing listing = QListing.listing;
        QUser mentor = new QUser("mentor");
        QUser mentee = new QUser("mentee");
        QSlot slot = QSlot.slot;

        List<Application> content = jpaQueryFactory
                .select(application)
                .from(application)
                .join(application.listing, listing).fetchJoin()
                .join(listing.mentor, mentor).fetchJoin()
                .join(application.mentee, mentee).fetchJoin()
                .join(application.slot, slot).fetchJoin()
                .where(
                        participantCondition,
                        statusEq(filter)
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(QApplication.application.count())
                .from(QApplication.application)
                .where(
                        participantCondition,
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
        Order direction = sortOrder.isAscending()
                        ? Order.ASC
                        : Order.DESC;
        return switch (sortOrder.getProperty()) {
            case "createdAt" -> new OrderSpecifier<>(direction, QApplication.application.createdAt);
            default -> new OrderSpecifier<>(Order.DESC, QApplication.application.createdAt);
        };
    }
    private BooleanExpression menteeIdEq(Long menteeId) {
        return QApplication.application.mentee.id.eq(menteeId);
    }
    private BooleanExpression mentorIdEq(Long mentorId) {
        return QApplication.application.listing.mentor.id.eq(mentorId);
    }
    private BooleanExpression statusEq(ApplicationFilter filter){
        return switch (filter){
            case PENDING -> QApplication.application.status.eq(ApplicationStatus.APPLIED);
            case PROCESSED -> QApplication.application.status.in(
                    ApplicationStatus.ACCEPTED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.CANCELED
            );
        };
    }
}
