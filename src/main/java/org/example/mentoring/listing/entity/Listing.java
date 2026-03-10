package org.example.mentoring.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "listings")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_user_id")
    private User mentor;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String topic;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING) // DB의 ENUM과 매핑
    @Column(nullable = false)
    private PlaceType placeType;

    private String placeDesc;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(precision = 3, scale = 2) // DECIMAL(3, 2)와 매핑
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Builder.Default
    private Integer reviewCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateStatus(ListingStatus newStatus) {
        if(!this.status.canChangeTo(newStatus)) {
            throw new BusinessException(ErrorCode.LISTING_INVALID_STATUS_TRANSITION);
        }
        this.status = newStatus;
    }

    public void changeTitle(String title) {
        if (title != null && !title.isBlank())  this.title = title;
    }
    public void changeTopic(String topic) {
        if (topic != null && !topic.isBlank())  this.topic = topic;
    }
    public void changePrice(Integer price) {
        if (price != null) this.price = price;
    }
    public void changeDescription(String description) {
        if (description != null && !description.isBlank())  this.description = description;
    }
    public void changePlace(PlaceType placeType, String placeDesc) {
        if (placeType != null) this.placeType = placeType;

        if (this.placeType == PlaceType.ONLINE) {
            this.placeDesc = null;
            return;
        }

        if (placeDesc != null && !placeDesc.isBlank()) {
            this.placeDesc = placeDesc;
        }
    }
}
