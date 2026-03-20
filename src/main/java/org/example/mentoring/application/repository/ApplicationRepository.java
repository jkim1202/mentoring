package org.example.mentoring.application.repository;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByMenteeIdAndSlotIdAndStatus(Long menteeId, Long slotId, ApplicationStatus applicationStatus);
}
