package org.example.mentoring.mentoringapplication.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.example.mentoring.mentoringapplication.domain.MentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentoringApplicationRepository extends JpaRepository<MentoringApplication, Long>, MentoringApplicationRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from MentoringApplication a where a.id = :applicationId")
    Optional<MentoringApplication> findByIdForUpdate(@Param("applicationId") Long applicationId);

    boolean existsByMenteeIdAndSlotIdAndStatus(Long menteeId, Long slotId, ApplicationStatus applicationStatus);

    @Query("""
            select a from MentoringApplication a
            join fetch a.listing l
            join fetch l.mentor
            join fetch a.mentee
            join fetch a.slot
            where a.id = :applicationId
            """)
    Optional<MentoringApplication> findDetailById(Long applicationId);

    @Query("""
    select a from MentoringApplication a
    where a.slot.id in :slotIds
      and a.status = org.example.mentoring.mentoringapplication.domain.ApplicationStatus.APPLIED
""")
    List<MentoringApplication> findAppliedApplicationsBySlotIds(@Param("slotIds") List<Long> slotIds);
}
