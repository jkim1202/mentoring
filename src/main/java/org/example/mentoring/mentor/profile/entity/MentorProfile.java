package org.example.mentoring.mentor.profile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mentoring.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mentor_profiles")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class MentorProfile {
    @Id
    private Long userId; // @MapsId에 의해 User의 ID와 공유됨

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // User 엔티티의 ID를 이 엔티티의 PK로 사용함
    @JoinColumn(name = "user_id") // DB 상의 외래 키 컬럼명
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Integer careerYears;

    private String major;

    private String currentCompany;

    // JSON 타입은 별도의 Converter나 라이브러리(Hypersistence Utils 등) 설정이 필요함
    @Column(columnDefinition = "json")
    private String specsJson;

    private String baseLocation;

    @Builder.Default
    private boolean verifiedFlag = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
