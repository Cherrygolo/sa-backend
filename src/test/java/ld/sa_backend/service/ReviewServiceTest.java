package ld.sa_backend.service;

import ld.sa_backend.entity.Customer;
import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.external.nlp.FeelingAnalyser;
import ld.sa_backend.repository.ReviewRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Classe de test unitaire pour ReviewService.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ReviewService reviewService;

    private Customer testCustomer;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("john@example.com", 1, "0600000000");
        testReview = generateTestReview(1, testCustomer, "Très bonne expérience!", null);
    }

    static Review generateTestReview(int id, Customer customer, String text, ReviewType type ) {
        Review review = new Review(1, customer, text);
        if (type != null) {
            review.setType(type);
        }
        return review;
    }



    //region ------------ TESTS FOR createReview METHOD ------------

    @Test
    void createReviewShouldThrowExceptionIfTextIsNull() {
        testReview.setText(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(testReview)
        );

        assertEquals("Review text cannot be null or empty.", ex.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReviewShouldThrowExceptionIfTextIsBlank() {
        testReview.setText("   ");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(testReview)
        );

        assertEquals("Review text cannot be null or empty.", ex.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReviewShouldCreateReviewSuccessfully() {
        // Arrange
        Customer savedCustomer = new Customer("john@example.com", 1, "0600000000");
        when(customerService.findOrCreateCustomer(any(Customer.class))).thenReturn(savedCustomer);

        // Mock statique pour FeelingAnalyser
        try (var mocked = Mockito.mockStatic(FeelingAnalyser.class)) {
            mocked.when(() -> FeelingAnalyser.analyzeFeelingType(testReview.getText()))
                  .thenReturn(ReviewType.POSITIVE);

            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review saved = invocation.getArgument(0);
                saved.setId(99);
                return saved;
            });

            // Act
            Review createdReview = reviewService.createReview(testReview);

            // Assert
            assertNotNull(createdReview);
            assertEquals(99, createdReview.getId());
            assertEquals(savedCustomer, createdReview.getCustomer());
            assertEquals(ReviewType.POSITIVE, createdReview.getType());

            verify(customerService).findOrCreateCustomer(testCustomer);
            verify(reviewRepository).save(testReview);
        }
    }

    //endregion

    //region ------------ TESTS FOR findReviews METHOD ------------

    @Test
    void findReviewsShouldReturnAllIfTypeIsNull() {
        testReview.setType(ReviewType.POSITIVE);
        Review negativeReview = generateTestReview(2, testCustomer, "Je suis assez déçu...", ReviewType.NEGATIVE );
        List<Review> reviews =  new ArrayList<>(Arrays.asList(testReview, negativeReview));
        when(reviewRepository.findAll()).thenReturn(reviews);

        List<Review> result = reviewService.findReviews(null);

        assertEquals(1, result.size());
        verify(reviewRepository).findAll();
        verify(reviewRepository, never()).findByType(any());
    }

    @Test
    void findReviewsShouldReturnFilteredByType() {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByType(ReviewType.NEGATIVE)).thenReturn(reviews);

        List<Review> result = reviewService.findReviews(ReviewType.NEGATIVE);

        assertEquals(1, result.size());
        verify(reviewRepository).findByType(ReviewType.NEGATIVE);
        verify(reviewRepository, never()).findAll();
    }

    //endregion

    //region ------------ TESTS FOR deleteReview METHOD ------------

    @Test
    void deleteReviewShouldThrowExceptionIfReviewDoesNotExist() {
        int nonExistentId = 123;
        when(reviewRepository.existsById(nonExistentId)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> reviewService.deleteReview(nonExistentId)
        );

        assertEquals("No review found with the ID : 123.", ex.getMessage());
        verify(reviewRepository).existsById(nonExistentId);
        verify(reviewRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteReviewShouldDeleteSuccessfullyIfExists() {
        int existingId = 1;
        when(reviewRepository.existsById(existingId)).thenReturn(true);

        reviewService.deleteReview(existingId);

        verify(reviewRepository).existsById(existingId);
        verify(reviewRepository).deleteById(existingId);
    }

    //endregion

}
