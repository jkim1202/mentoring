package org.example.mentoring.mentor.infrastructure.repository;

import org.example.mentoring.mentor.domain.MentorProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<MentorProfile> findDetailByUserId(Long userId);
}
