package ld.sa_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ld.sa_backend.entity.Review;
import java.util.List;
import ld.sa_backend.enums.ReviewType;


public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByType(ReviewType type);

}
