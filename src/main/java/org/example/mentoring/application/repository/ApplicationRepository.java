package org.example.mentoring.application.repository;

import org.example.mentoring.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByMenteeIdAndSlotId(Long menteeId, Long slotId);
}
