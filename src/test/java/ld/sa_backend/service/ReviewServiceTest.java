package ld.sa_backend.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.external.nlp.FeelingAnalyser;
import ld.sa_backend.repository.ReviewRepository;
import ld.sa_backend.testutils.CustomerTestBuilder;
import ld.sa_backend.testutils.ReviewTestBuilder;
import ld.sa_backend.testutils.TestDataFactory;

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


    //region ------------ CREATE REVIEW ------------

    @Test
    void createReview_shouldThrowException_whenTextIsNull() {
        Review reviewToCreate = ReviewTestBuilder.aReview()
            .withText(null)
            .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(reviewToCreate)
        );

        assertEquals("Review text cannot be null or empty.", exception.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReview_shouldThrowException_whenTextIsBlank() {
        Review reviewToCreate = ReviewTestBuilder.aReview()
            .withText("   ")
            .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(reviewToCreate)
        );

        assertEquals("Review text cannot be null or empty.", exception.getMessage());
        verifyNoInteractions(customerService, reviewRepository);
    }

    @Test
    void createReview_shouldCreateReviewSuccessfully_whenDataIsValid() {
        Customer customerFromRequest = TestDataFactory.createDefaultCustomer();

        Review reviewToCreate = ReviewTestBuilder.aReview()
            .withCustomer(customerFromRequest)
            .withText("Très bonne expérience !")
            .build();

        Customer savedCustomer = CustomerTestBuilder.aCustomer()
            .withId(1)
            .withEmail(customerFromRequest.getEmail())
            .build();

        when(customerService.findOrCreateCustomer(customerFromRequest))
            .thenReturn(savedCustomer);

        try (var mockedFeelingAnalyser = Mockito.mockStatic(FeelingAnalyser.class)) {
            mockedFeelingAnalyser
                .when(() -> FeelingAnalyser.analyzeFeelingType(reviewToCreate.getText()))
                .thenReturn(ReviewType.POSITIVE);

            when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review savedReview = invocation.getArgument(0);
                    savedReview.setId(99);
                    return savedReview;
                });

            Review createdReview = reviewService.createReview(reviewToCreate);

            assertNotNull(createdReview);
            assertEquals(99, createdReview.getId());
            assertEquals(savedCustomer, createdReview.getCustomer());
            assertEquals(ReviewType.POSITIVE, createdReview.getType());

            verify(customerService).findOrCreateCustomer(customerFromRequest);
            verify(reviewRepository).save(reviewToCreate);
        }
    }

    //endregion

    //region ------------ FIND REVIEWS ------------

    @Test
    void findReviews_shouldReturnAllReviews_whenTypeIsNull() {
        Customer customer = TestDataFactory.createDefaultCustomer();

        Review positiveReview = ReviewTestBuilder.aReview()
            .withId(1)
            .withCustomer(customer)
            .withText("Excellent service")
            .withType(ReviewType.POSITIVE)
            .build();

        Review negativeReview = ReviewTestBuilder.aReview()
            .withId(2)
            .withCustomer(customer)
            .withText("Très déçu")
            .withType(ReviewType.NEGATIVE)
            .build();

        List<Review> existingReviews = List.of(positiveReview, negativeReview);

        when(reviewRepository.findAll()).thenReturn(existingReviews);

        List<Review> foundReviews = reviewService.findReviews(null);

        assertEquals(2, foundReviews.size());
        verify(reviewRepository).findAll();
        verify(reviewRepository, never()).findByType(any());
    }

    @Test
    void findReviews_shouldReturnFilteredReviews_whenTypeIsProvided() {
        Customer customer = TestDataFactory.createDefaultCustomer();

        Review negativeReview1 = ReviewTestBuilder.aReview()
            .withId(1)
            .withCustomer(customer)
            .withText("Très déçu")
            .withType(ReviewType.NEGATIVE)
            .build();

        Review negativeReview2 = ReviewTestBuilder.aReview()
            .withId(2)
            .withCustomer(customer)
            .withText("Service médiocre")
            .withType(ReviewType.NEGATIVE)
            .build();

        Review positiveReview = ReviewTestBuilder.aReview()
            .withId(3)
            .withCustomer(customer)
            .withText("Excellent")
            .withType(ReviewType.POSITIVE)
            .build();

        when(reviewRepository.findByType(ReviewType.NEGATIVE))
            .thenReturn(List.of(negativeReview1, negativeReview2));

        List<Review> foundReviews = reviewService.findReviews(ReviewType.NEGATIVE);

        assertEquals(2, foundReviews.size());
        assertTrue(foundReviews.stream().allMatch(
            review -> review.getType() == ReviewType.NEGATIVE
        ));

        verify(reviewRepository).findByType(ReviewType.NEGATIVE);
        verify(reviewRepository, never()).findAll();
    }

    //endregion

    //region ------------ DELETE REVIEW ------------

    @Test
    void deleteReview_shouldThrowException_whenReviewDoesNotExist() {
        int nonExistentReviewId = 123;

        when(reviewRepository.existsById(nonExistentReviewId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> reviewService.deleteReview(nonExistentReviewId)
        );

        assertEquals(
            "No review found with the ID : " + nonExistentReviewId + ".",
            exception.getMessage()
        );

        verify(reviewRepository).existsById(nonExistentReviewId);
        verify(reviewRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteReview_shouldDeleteReview_whenReviewExists() {
        int existingReviewId = 1;

        when(reviewRepository.existsById(existingReviewId)).thenReturn(true);

        reviewService.deleteReview(existingReviewId);

        verify(reviewRepository).existsById(existingReviewId);
        verify(reviewRepository).deleteById(existingReviewId);
    }

    //endregion
}