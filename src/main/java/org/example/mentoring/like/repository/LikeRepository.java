package org.example.mentoring.like.repository;

import org.example.mentoring.like.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like,Long> {
    @EntityGraph(attributePaths = "listing")
    Page<Like> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Like> findByUserIdAndListingId(Long userId, Long listingId);

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    @Query("""
            select lk.listing.id
            from Like lk
            where lk.user.id = :userId
              and lk.listing.id in :listingIds
            """)
    Set<Long> findLikedListingIdsByUserIdAndListingIds(Long userId, Collection<Long> listingIds);
}
