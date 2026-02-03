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

        if (review.getText() == null || review.getText().isBlank()) {
            throw new IllegalArgumentException("Review text cannot be null or empty.");
        }

        Customer customer = review.getCustomer();
        if (customer == null) {
            throw new IllegalArgumentException("Customer info must be provided.");
        }

        // Si l'ID est présent, customer existant
        if (customer.getId() != null) {
            customer = customerService.getCustomerById(customer.getId());
        } else {
            // Sinon, création du customer si email présent
            if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required to create a new review");
            }
            customer = customerService.findOrCreateCustomer(customer);
        }

        review.setCustomer(customer);

        // Analyse du type
        review.setType(FeelingAnalyser.analyzeFeelingType(review.getText()));

        return reviewRepository.save(review);
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