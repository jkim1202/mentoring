package org.example.mentoring.review.repository;

import org.example.mentoring.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long reservationId);
}
