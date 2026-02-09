package ld.sa_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;


public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByType(ReviewType type);
    boolean existsByCustomerId(int customerId);

}
