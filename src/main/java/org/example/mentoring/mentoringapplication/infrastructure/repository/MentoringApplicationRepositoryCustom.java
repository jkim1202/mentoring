package org.example.mentoring.mentoringapplication.infrastructure.repository;

import org.example.mentoring.mentoringapplication.domain.MentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentoringApplicationRepositoryCustom {
    Page<MentoringApplication> searchByMentorId(ApplicationFilter filter, Pageable pageable, Long mentorId);
    Page<MentoringApplication> searchByMenteeId(ApplicationFilter filter, Pageable pageable, Long menteeId);

}
