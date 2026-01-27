/**
 * Builder de test pour {@link Customer}.
 *
 * Permet de créer des Customer de test lisibles et configurables.
 */

package ld.sa_backend.testutils;

import ld.sa_backend.entity.Customer;

public class CustomerTestBuilder {
    private String email = "customer@test.com";
    private Integer id = 1;
    private String phone = "0600000000";
    
    public static CustomerTestBuilder aCustomer() {
        return new CustomerTestBuilder();
    }
    
    public CustomerTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public CustomerTestBuilder withId(Integer id) {
        this.id = id;
        return this;
    }
    
    public CustomerTestBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }
    
    public Customer build() {
        return new Customer(email, id, phone);
    }
}