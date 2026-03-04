package org.example.mentoring.mentor.profile.repository;

import org.example.mentoring.mentor.profile.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
}
