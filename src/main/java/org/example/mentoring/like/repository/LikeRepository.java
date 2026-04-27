package org.example.mentoring.like.repository;

import org.example.mentoring.like.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {
    @EntityGraph(attributePaths = "listing")
    Page<Like> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Like> findByUserIdAndListingId(Long userId, Long listingId);
}
