package ld.feeltrack_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ld.feeltrack_backend.entity.Customer;


public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    
    Customer findByEmail(String email);

}
