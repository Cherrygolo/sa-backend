package ld.sa_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // Méthode utilitaire pour créer un client de test
    private Customer createTestCustomer() {
        Customer customer = new Customer("john@example.com", 1, "0600000000");
        return customer;
    }

    //region ------------ TESTS FOR CustomerService.createCustomer METHOD ------------

    @Test
    void createCustomerShouldSaveWhenEmailNotUsed() {
        Customer customer = createTestCustomer();

        // Simulation du comportement du repository avec le customer qui n'existe pas encore
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(null);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer resultingCustomer = customerService.createCustomer(customer);

        assertEquals("john@example.com", resultingCustomer.getEmail());
        assertEquals("0600000000", resultingCustomer.getPhone());
        assertEquals(1, resultingCustomer.getId());
        // Vérification que la méthode save a été appelée
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomerShouldThrowExceptionWhenEmailAlreadyUsed() {

        Customer customer = createTestCustomer();

        // Simulation du comportement du repository avec le customer qui existe déjà
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(customer);

        try {
            customerService.createCustomer(customer);
        } catch (DataIntegrityViolationException ex) {
            assertEquals(
                "An user already exists with the email address : " + customer.getEmail(),
                ex.getMessage()
            );
        }
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.deleteCustomer METHOD ------------

    @Test
    void deleteCustomerShouldCallDeleteWhenCustomerExists() {
        Customer customer = createTestCustomer();

        // Simulation du comportement du repository pour indiquer que le client existe
        when(customerRepository.existsById(customer.getId())).thenReturn(true);

        customerService.deleteCustomer(customer.getId());

        // Vérification que la méthode deleteById a été appelée
        verify(customerRepository).deleteById(customer.getId());
    }

    @Test
    void deleteCustomerShouldThrowExceptionWhenCustomerDoesNotExist() {
        int nonExistentCustomerId = 999;

        when(customerRepository.existsById(nonExistentCustomerId)).thenReturn(false);

        try {
            customerService.deleteCustomer(nonExistentCustomerId);

        } catch (EntityNotFoundException ex) {
            assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
        }
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.getAllCustomers METHOD ------------

    @Test
    void getAllCustomersShouldReturnsListWithAllExistingCustomers() {
        Customer customer1 = createTestCustomer();
        Customer customer2 = new Customer("emma@test.com", 2 , "0700000000");
        List<Customer> existingCustomers = new ArrayList<>(Arrays.asList(customer1, customer2));

        // Simulation du comportement du repository avec deux clients existants
        when(customerRepository.findAll()).thenReturn(existingCustomers);

        List<Customer> foundCustomers = this.customerService.getAllCustomers();

        //Vérification des clients récupérés
        assertEquals(existingCustomers.size(), foundCustomers.size());
        for (Integer i = 0 ; i < existingCustomers.size() ; i++) {
            Customer existingCustomer = existingCustomers.get(i);
            Customer foundCustomer = foundCustomers.get(i);

            assertEquals(existingCustomer.getId(), foundCustomer.getId());
            assertEquals(existingCustomer.getEmail(), foundCustomer.getEmail());
            assertEquals(existingCustomer.getPhone(), foundCustomer.getPhone());
        }
        // Vérification que la méthode findAll a bien été appellée
        verify(customerRepository).findAll();
    }


    @Test
    void getAllCustomersShouldReturnEmptyListWhenNoCustomersExist() {
        // Simulation du comportement du repository avec aucun customer existant
        when(customerRepository.findAll()).thenReturn(List.of());
        List<Customer> foundCustomers = customerService.getAllCustomers();

        assertTrue(foundCustomers.isEmpty());
        verify(customerRepository).findAll();
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.getCustomerById METHOD ------------

    @Test
    void getCustomerByIdShouldReturnsMatchingCustomerIfExists() {
        // Simulation du comportement du repository avec un client existant
        Customer customer = createTestCustomer();
        int customerId = customer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Customer foundCustomer = customerService.getCustomerById(customerId);

        assertEquals(customer.getEmail(), foundCustomer.getEmail());
        assertEquals(customer.getPhone(), foundCustomer.getPhone());
        assertEquals(customer.getId(), foundCustomer.getId());

        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerByIdShouldThrowsExceptionIfNoMatchingCustomerExists() {
        // Simulation du comportement du repository avec aucun client existant avec l'id
        int nonExistentCustomerId = 111;
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        try {
            customerService.getCustomerById(nonExistentCustomerId);
        } catch (EntityNotFoundException ex) {
            assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
            verify(customerRepository).findById(nonExistentCustomerId);
        }
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.findOrCreateCustomer METHOD ------------

    @Test
    void findOrCreateCustomerShouldReturnsMatchingCustomerIfExists() {
        // Simulation du comportement du repository avec un client existant avec une adresse email
        Customer existingCustomer = createTestCustomer();
        when(customerRepository.findByEmail(existingCustomer.getEmail())).thenReturn(existingCustomer);

        Customer usedCustomer = new Customer();
        usedCustomer.setEmail(existingCustomer.getEmail());
        Customer resultingCustomer = customerService.findOrCreateCustomer(usedCustomer);

        // Vérification que le contact renvoyé est celui déjà existant avec la même adresse email
        assertEquals(existingCustomer.getEmail(), resultingCustomer.getEmail());
        assertEquals(existingCustomer.getPhone(), resultingCustomer.getPhone());
        assertEquals(existingCustomer.getId(), resultingCustomer.getId());

        verify(customerRepository).findByEmail(usedCustomer.getEmail());
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void findOrCreateCustomerShouldCreateGivenCustomerIfNotExists() {
        // Simulation du comportement du repository avec aucun client existant avec l'adresse email testée
        Customer customerToCreate = new Customer();
        when(customerRepository.findByEmail(customerToCreate.getEmail())).thenReturn(null);
        when(customerRepository.save(customerToCreate)).thenReturn(customerToCreate);

        Customer foundCustomer = customerService.findOrCreateCustomer(customerToCreate);

        // Vérification que le contact renvoyé est le contact passé en paramètre venant d'être créé 
        assertEquals(customerToCreate.getEmail(), foundCustomer.getEmail());
        assertEquals(customerToCreate.getPhone(), foundCustomer.getPhone());
        assertEquals(customerToCreate.getId(), foundCustomer.getId());
        verify(customerRepository).findByEmail(customerToCreate.getEmail());
        verify(customerRepository).save(customerToCreate);
        verifyNoMoreInteractions(customerRepository);

    }

    //endregion

    //region ------------ TESTS FOR CustomerService.updateCustomer METHOD ------------

    @Test
    void updateCustomerShouldThrowExceptionIfGivenCustomerDoesNotExist() {
        // Simulation du comportement du repository avec aucun client existant avec l'id
        Customer nonExistentCustomer = createTestCustomer();
        int nonExistentCustomerId = 111;
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        try {
            customerService.updateCustomer(nonExistentCustomerId, nonExistentCustomer);
        } catch (EntityNotFoundException ex) {
            assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
            verify(customerRepository).findById(nonExistentCustomerId);
            verifyNoMoreInteractions(customerRepository);
        }
    }

    @Test
    void updateCustomerShouldThrowExceptionIfEmailAlreadyUsedByAnotherCustomer() {
        Customer existingCustomer = createTestCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(id);
        updatedCustomer.setEmail("duplicate@example.com");
        updatedCustomer.setPhone("0600000000");

        Customer otherCustomer = new Customer();
        otherCustomer.setId(999);
        otherCustomer.setEmail("duplicate@example.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail("duplicate@example.com")).thenReturn(otherCustomer);

        DataIntegrityViolationException ex = assertThrows(
            DataIntegrityViolationException.class,
            () -> customerService.updateCustomer(id, updatedCustomer)
        );

        assertTrue(ex.getMessage().contains("An user already exists with the email address"));
        verify(customerRepository).findById(id);
        verify(customerRepository).findByEmail("duplicate@example.com");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomerShouldThrowExceptionIfIdMismatch() {
        Customer existingCustomer = createTestCustomer();
        int idInPath = 1;
        int idInBody = 2;
        existingCustomer.setId(idInBody);

        when(customerRepository.findById(idInPath)).thenReturn(Optional.of(existingCustomer));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.updateCustomer(idInPath, existingCustomer)
        );

        assertTrue(ex.getMessage().contains("Client ID mismatch"));
        verify(customerRepository).findById(idInPath);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void updateExistingCustomerShouldUpdatePhoneSuccessfullyIfEmailNotChanged() {
        Customer existingCustomer = createTestCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(id);
        updatedCustomer.setEmail("john@example.com");
        updatedCustomer.setPhone("0700000000");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail("john@example.com")).thenReturn(existingCustomer);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.updateCustomer(id, updatedCustomer);

        assertEquals("john@example.com", result.getEmail());
        assertEquals("0700000000", result.getPhone());
        verify(customerRepository).findById(id);
        verify(customerRepository).findByEmail("john@example.com");
        verify(customerRepository).save(existingCustomer);
    }

    @Test
    void updateExistingCustomerShouldUpdateEmailAndPhoneSuccessfully() {
        Customer existingCustomer = createTestCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(id);
        updatedCustomer.setEmail("new.email@example.com");
        updatedCustomer.setPhone("0600000000");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail("new.email@example.com")).thenReturn(null);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.updateCustomer(id, updatedCustomer);

        assertEquals("new.email@example.com", result.getEmail());
        assertEquals("0600000000", result.getPhone());
        verify(customerRepository).findById(id);
        verify(customerRepository).findByEmail("new.email@example.com");
        verify(customerRepository).save(existingCustomer);
    }

    //endregion

}
