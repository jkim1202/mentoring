package org.example.mentoring.listing.entity;

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
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "slots")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계 - 여러 슬롯은 하나의 게시글에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false) // FK 컬럼 이름
    private Listing listing;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status; // OPEN, BOOKED, EXPIRED

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void book(){
        if(this.status == SlotStatus.OPEN) {
            this.status = SlotStatus.BOOKED;
        } else  {
            throw new BusinessException(ErrorCode.SLOT_INVALID_STATUS_TRANSITION);
        }
    }
    public void reopen(){
        if(this.status == SlotStatus.BOOKED) {
            this.status = SlotStatus.OPEN;
        } else  {
            throw new BusinessException(ErrorCode.SLOT_INVALID_STATUS_TRANSITION);
        }
    }

    public void expire() {
        if (this.status.canChangeTo(SlotStatus.EXPIRED)) {
            this.status = SlotStatus.EXPIRED;
        } else {
            throw new BusinessException(ErrorCode.SLOT_INVALID_STATUS_TRANSITION);
        }
    }

    public boolean isStarted() {
        return !LocalDateTime.now().isBefore(this.startAt);
    }
}
