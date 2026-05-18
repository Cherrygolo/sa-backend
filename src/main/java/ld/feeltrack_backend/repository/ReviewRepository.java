package ld.feeltrack_backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ld.feeltrack_backend.entity.Review;
import ld.feeltrack_backend.enums.ReviewType;
import ld.feeltrack_backend.projection.ReviewCountProjection;
import ld.feeltrack_backend.projection.ReviewTimelineProjection;


public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findAllByOrderByCreatedAtDesc();
    List<Review> findByType(ReviewType type);
    List<Review> findByTypeOrderByCreatedAtDesc(ReviewType type);
    
    boolean existsByCustomerId(int customerId);

    @Query("""
    SELECT r.type AS type, COUNT(r) AS count
    FROM Review r
    GROUP BY r.type
    """)
    List<ReviewCountProjection> countReviewsByType();

    // Timeline query to get daily counts of reviews by type for the last N days
    @Query("""
    SELECT r.createdDate AS createdDate,
        r.type AS type,
        COUNT(r) AS count
    FROM Review r
    WHERE r.createdDate >= :from
    GROUP BY r.createdDate, r.type
    ORDER BY r.createdDate
    """)
    List<ReviewTimelineProjection> getTimeline(LocalDate from);
    

}
