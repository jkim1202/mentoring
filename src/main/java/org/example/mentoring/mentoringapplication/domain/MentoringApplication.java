package org.example.mentoring.mentoringapplication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.listing.domain.Listing;
import org.example.mentoring.slot.domain.Slot;
import org.example.mentoring.user.domain.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "applications")
@EntityListeners(AuditingEntityListener.class)
public class MentoringApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @JoinColumn(name = "listing_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Listing listing;

    @JoinColumn(name = "mentee_user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User mentee;

    @JoinColumn(name = "slot_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Slot slot;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void changeStatus(ApplicationStatus newStatus) {
        if (this.status.canChangeTo(newStatus))
            this.status = newStatus;
        else throw new BusinessException(ErrorCode.APPLICATION_INVALID_STATUS_TRANSITION);
    }
}
