package ld.sa_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {

        Customer foundCustomerInDatabase = this.customerRepository.findByEmail(customer.getEmail());

        if (foundCustomerInDatabase != null) {
            throw new DataIntegrityViolationException(
                "An user already exists with the email address : " + customer.getEmail()
            );
        }
        // save() renvoie l'entité créée, avec son ID généré
        return this.customerRepository.save(customer);

    }

    public void deleteCustomer(int id) {
        // Vérification que l'utilisateur existe avant de tenter de le supprimer
        if (!this.customerRepository.existsById(id)) {
            throw new EntityNotFoundException("No customer found with the ID : " + id + ".");
        }
        this.customerRepository.deleteById(id);
    }

    public List<Customer> getAllCustomers() {
        return this.customerRepository.findAll();
    }

    public Customer getCustomerById(int id) {
        Optional<Customer> existingCustomer = this.customerRepository.findById(id);
        // retourne le résultat de optionCustomer.get() si existingCustomer est présent
        return existingCustomer.orElseThrow(
            () -> new EntityNotFoundException("No customer found with the ID : " + id + ".")
        );
    }

    public Customer findOrCreateCustomer(Customer customer) {
        Customer foundCustomerInDatabase = this.customerRepository.findByEmail(customer.getEmail());

        if (foundCustomerInDatabase == null) {
            return this.customerRepository.save(customer);
        }
        
        return foundCustomerInDatabase;
    }

    public Customer updateCustomer(int id, Customer updatedCustomer) {
        Customer foundCustomerInDatabase = this.getCustomerById(id);

        if (id != updatedCustomer.getId()) {
            throw new IllegalArgumentException(
                "Client ID mismatch: path ID = " + id + ", request body ID = " + updatedCustomer.getId() + "."
            );
        }

        // Vérifier si l'email est déjà utilisé par un autre client
        Customer existingCustomerWithEmail = customerRepository.findByEmail(updatedCustomer.getEmail());
        if (existingCustomerWithEmail != null && ( existingCustomerWithEmail.getId() != id ) ) {
            throw new DataIntegrityViolationException(
                "An user already exists with the email address : " + updatedCustomer.getEmail()
            );
        }

        foundCustomerInDatabase.setEmail(updatedCustomer.getEmail());
        foundCustomerInDatabase.setPhone(updatedCustomer.getPhone());
        return customerRepository.save(foundCustomerInDatabase);

    }

}