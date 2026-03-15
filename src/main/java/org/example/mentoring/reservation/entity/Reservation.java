package org.example.mentoring.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reservations_application_id", columnNames = "application_id"),
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_user_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_user_id", nullable = false)
    private User mentee;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void changeStatus(ReservationStatus newStatus) {
        if(this.status.canChangeTo(newStatus))
            this.status = newStatus;
        else
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS_TRANSITION);
    }
}
