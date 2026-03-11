package org.example.mentoring.mentor.profile.repository;

import org.example.mentoring.mentor.profile.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
}
