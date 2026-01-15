package ld.sa_backend.service;

import ld.sa_backend.entity.Customer;
import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.external.nlp.FeelingAnalyser;
import ld.sa_backend.repository.ReviewRepository;
import ld.sa_backend.testutils.CustomerTestBuilder;
import ld.sa_backend.testutils.ReviewTestBuilder;
import ld.sa_backend.testutils.TestDataFactory;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    //region ------------ TESTS FOR createReview METHOD ------------

    @Test
    void createReviewShouldThrowExceptionIfTextIsNull() {
        Review testReview = ReviewTestBuilder.aReview()
            .withText(null)
            .build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(testReview)
        );

        assertEquals("Review text cannot be null or empty.", ex.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReviewShouldThrowExceptionIfTextIsBlank() {
        Review testReview = ReviewTestBuilder.aReview()
            .withText("   ")
            .build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(testReview)
        );

        assertEquals("Review text cannot be null or empty.", ex.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReviewShouldCreateReviewSuccessfully() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();
        Review testReview = ReviewTestBuilder.aReview()
            .withId(1)
            .withCustomer(testCustomer)
            .withText("Très bonne expérience!")
            .build();

        Customer savedCustomer = CustomerTestBuilder.aCustomer()
            .withEmail("john@example.com")
            .withId(1)
            .withPhone("0600000000")
            .build();

        when(customerService.findOrCreateCustomer(any(Customer.class))).thenReturn(savedCustomer);

        try (var mocked = Mockito.mockStatic(FeelingAnalyser.class)) {
            mocked.when(() -> FeelingAnalyser.analyzeFeelingType(testReview.getText()))
                  .thenReturn(ReviewType.POSITIVE);

            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review saved = invocation.getArgument(0);
                saved.setId(99);
                return saved;
            });

            Review createdReview = reviewService.createReview(testReview);

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
        Customer testCustomer = TestDataFactory.createDefaultCustomer();
        
        Review positiveReview = ReviewTestBuilder.aReview()
            .withId(1)
            .withCustomer(testCustomer)
            .withText("Très bonne expérience!")
            .withType(ReviewType.POSITIVE)
            .build();

        Review negativeReview = ReviewTestBuilder.aReview()
            .withId(2)
            .withCustomer(testCustomer)
            .withText("Je suis assez déçu...")
            .withType(ReviewType.NEGATIVE)
            .build();

        List<Review> allReviews = List.of(positiveReview, negativeReview);
        when(reviewRepository.findAll()).thenReturn(allReviews);

        List<Review> result = reviewService.findReviews(null);

        assertEquals(2, result.size());
        verify(reviewRepository).findAll();
        verify(reviewRepository, never()).findByType(any());
    }

    @Test
    void findReviewsShouldReturnFilteredByType() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();
        
        Review negativeReview = ReviewTestBuilder.aReview()
            .withId(1)
            .withCustomer(testCustomer)
            .withText("Je suis assez déçu...")
            .withType(ReviewType.NEGATIVE)
            .build();

        List<Review> negativeReviews = List.of(negativeReview);
        when(reviewRepository.findByType(ReviewType.NEGATIVE)).thenReturn(negativeReviews);

        List<Review> result = reviewService.findReviews(ReviewType.NEGATIVE);

        assertEquals(1, result.size());
        assertEquals(ReviewType.NEGATIVE, result.get(0).getType());
        assertEquals("Je suis assez déçu...", result.get(0).getText());
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