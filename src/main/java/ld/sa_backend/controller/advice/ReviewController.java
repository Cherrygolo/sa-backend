package ld.sa_backend.controller.advice;

import ld.sa_backend.entity.Review;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.service.ReviewService;

import java.util.List;

import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping( path = "review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Review> createReview (@RequestBody Review review) {
        Review createdReview = this.reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }
    
    @GetMapping
    public List<Review> findReviews(@RequestParam(required = false) ReviewType type) {
        return this.reviewService.findReviews(type);
    }
    
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "{id}")
    public void deleteReview(@PathVariable int id) {
        this.reviewService.deleteReview(id);
    }
    

}
