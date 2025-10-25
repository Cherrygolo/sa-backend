package ld.sa_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.external.nlp.FeelingAnalyser;
import ld.sa_backend.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerService customerService;

    public ReviewService(CustomerService customerService, ReviewRepository reviewRepository) {
        this.customerService = customerService;
        this.reviewRepository = reviewRepository;
    }
    
    public Review createReview(Review review) {

        String reviewText = review.getText();

        if (reviewText == null || reviewText.isBlank()) {
            throw new IllegalArgumentException("Review text cannot be null or empty.");
        }

        // Vérification de l'existence de l'avis
        Customer reviewCustomer = this.customerService.findOrCreateCustomer(review.getCustomer());
        review.setCustomer(reviewCustomer);

        // Analyse du type de sentiment
        ReviewType reviewType = FeelingAnalyser.analyzeFeelingType(review.getText());
        review.setType(reviewType);

        // save() renvoie l'entité créée, avec son ID généré
        return this.reviewRepository.save(review);
    }
    
    public List<Review> findReviews(ReviewType reviewType) {

        if (reviewType == null) {
            return this.reviewRepository.findAll();
        }
        
        return this.reviewRepository.findByType(reviewType);
    }

    public void deleteReview(int id) {
        // Vérification que l'avis existe avant de tenter de le supprimer
        if (!this.reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("No review found with the ID : " + id + ".");
        }
        this.reviewRepository.deleteById(id);
    }

}