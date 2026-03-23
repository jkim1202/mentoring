package org.example.mentoring.application.repository;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.type.ApplicationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationRepositoryCustom {
    Page<Application> searchByMentorId(ApplicationFilter filter, Pageable pageable, Long mentorId);
    Page<Application> searchByMenteeId(ApplicationFilter filter, Pageable pageable, Long menteeId);

}
