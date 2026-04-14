/**
 * Builder de test pour {@link Review}.
 *
 * Permet de créer des Review de test lisibles et configurables.
 * À utiliser lorsque le test nécessite un contrôle précis des champs.
 */

package ld.feeltrack_backend.testutils;

import ld.feeltrack_backend.entity.Customer;
import ld.feeltrack_backend.entity.Review;
import ld.feeltrack_backend.enums.ReviewType;

public class ReviewTestBuilder {
    private Integer id = null;
    private Customer customer = TestDataFactory.createDefaultCustomer();
    private String text = "Très bonne expérience!";
    private ReviewType type = null;
    
    public static ReviewTestBuilder aReview() {
        return new ReviewTestBuilder();
    }
    
    public ReviewTestBuilder withId(Integer id) {
        this.id = id;
        return this;
    }
    
    public ReviewTestBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }
    
    public ReviewTestBuilder withText(String text) {
        this.text = text;
        return this;
    }
    
    public ReviewTestBuilder withType(ReviewType type) {
        this.type = type;
        return this;
    }
    
    public Review build() {
        Review review = new Review(customer, text);
        if (id != null) {
            review.setId(id);
        }
        if (type != null) {
            review.setType(type);
        }
        return review;
    }
}
