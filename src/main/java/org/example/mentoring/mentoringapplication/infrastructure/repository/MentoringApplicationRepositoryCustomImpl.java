package org.example.mentoring.mentoringapplication.infrastructure.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.mentoring.mentoringapplication.domain.MentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationStatus;
import org.example.mentoring.mentoringapplication.domain.QMentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationFilter;
import org.example.mentoring.listing.domain.QListing;
import org.example.mentoring.slot.domain.QSlot;
import org.example.mentoring.user.domain.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class MentoringApplicationRepositoryCustomImpl implements MentoringApplicationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public MentoringApplicationRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<MentoringApplication> searchByMentorId(ApplicationFilter filter, Pageable pageable, Long mentorId) {
        return searchApplications(mentorIdEq(mentorId), filter, pageable);
    }

    @Override
    public Page<MentoringApplication> searchByMenteeId(ApplicationFilter filter, Pageable pageable, Long menteeId) {
        return searchApplications(menteeIdEq(menteeId), filter, pageable);
    }

    private Page<MentoringApplication> searchApplications(
            BooleanExpression participantCondition,
            ApplicationFilter filter,
            Pageable pageable
    ) {
        QMentoringApplication application = QMentoringApplication.mentoringApplication;
        QListing listing = QListing.listing;
        QUser mentor = new QUser("mentor");
        QUser mentee = new QUser("mentee");
        QSlot slot = QSlot.slot;

        List<MentoringApplication> content = jpaQueryFactory
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
                .select(QMentoringApplication.mentoringApplication.count())
                .from(QMentoringApplication.mentoringApplication)
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
            case "createdAt" -> new OrderSpecifier<>(direction, QMentoringApplication.mentoringApplication.createdAt);
            default -> new OrderSpecifier<>(Order.DESC, QMentoringApplication.mentoringApplication.createdAt);
        };
    }
    private BooleanExpression menteeIdEq(Long menteeId) {
        return QMentoringApplication.mentoringApplication.mentee.id.eq(menteeId);
    }
    private BooleanExpression mentorIdEq(Long mentorId) {
        return QMentoringApplication.mentoringApplication.listing.mentor.id.eq(mentorId);
    }
    private BooleanExpression statusEq(ApplicationFilter filter){
        return switch (filter){
            case PENDING -> QMentoringApplication.mentoringApplication.status.eq(ApplicationStatus.APPLIED);
            case PROCESSED -> QMentoringApplication.mentoringApplication.status.in(
                    ApplicationStatus.ACCEPTED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.CANCELED
            );
        };
    }
}
