package org.example.mentoring.application.repository;

import jakarta.persistence.LockModeType;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, ApplicationRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Application a where a.id = :applicationId")
    Optional<Application> findByIdForUpdate(@Param("applicationId") Long applicationId);

    boolean existsByMenteeIdAndSlotIdAndStatus(Long menteeId, Long slotId, ApplicationStatus applicationStatus);

    @Query("""
            select a from Application a
            join fetch a.listing l
            join fetch l.mentor
            join fetch a.mentee
            join fetch a.slot
            where a.id = :applicationId
            """)
    Optional<Application> findDetailById(Long applicationId);

    @Query("""
    select a from Application a
    where a.slot.id in :slotIds
      and a.status = org.example.mentoring.application.entity.ApplicationStatus.APPLIED
""")
    List<Application> findAppliedApplicationsBySlotIds(@Param("slotIds") List<Long> slotIds);
}
