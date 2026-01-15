package ld.sa_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import ld.sa_backend.testutils.CustomerTestBuilder;
import ld.sa_backend.testutils.TestDataFactory;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    //region ------------ TESTS FOR CustomerService.createCustomer METHOD ------------

    @Test
    void createCustomerShouldSaveWhenEmailNotUsed() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();

        when(customerRepository.findByEmail(testCustomer.getEmail())).thenReturn(null);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        Customer resultingCustomer = customerService.createCustomer(testCustomer);

        assertEquals(testCustomer.getEmail(), resultingCustomer.getEmail());
        assertEquals(testCustomer.getPhone(), resultingCustomer.getPhone());
        assertEquals(testCustomer.getId(), resultingCustomer.getId());
        verify(customerRepository).findByEmail(testCustomer.getEmail());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void createCustomerShouldThrowExceptionWhenEmailAlreadyUsed() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();

        when(customerRepository.findByEmail(testCustomer.getEmail())).thenReturn(testCustomer);

        DataIntegrityViolationException ex = assertThrows(
            DataIntegrityViolationException.class,
            () -> customerService.createCustomer(testCustomer)
        );

        assertEquals(
            "An user already exists with the email address : " + testCustomer.getEmail(),
            ex.getMessage()
        );
        verify(customerRepository).findByEmail(testCustomer.getEmail());
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.deleteCustomer METHOD ------------

    @Test
    void deleteCustomerShouldCallDeleteWhenCustomerExists() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();

        when(customerRepository.existsById(testCustomer.getId())).thenReturn(true);

        customerService.deleteCustomer(testCustomer.getId());

        verify(customerRepository).existsById(testCustomer.getId());
        verify(customerRepository).deleteById(testCustomer.getId());
    }

    @Test
    void deleteCustomerShouldThrowExceptionWhenCustomerDoesNotExist() {
        int nonExistentCustomerId = 999;

        when(customerRepository.existsById(nonExistentCustomerId)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.deleteCustomer(nonExistentCustomerId)
        );

        assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
        verify(customerRepository).existsById(nonExistentCustomerId);
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.getAllCustomers METHOD ------------

    @Test
    void getAllCustomersShouldReturnsListWithAllExistingCustomers() {
        List<Customer> existingCustomers = TestDataFactory.createCustomerList(2);

        when(customerRepository.findAll()).thenReturn(existingCustomers);

        List<Customer> foundCustomers = customerService.getAllCustomers();

        assertEquals(existingCustomers.size(), foundCustomers.size());
        for (int i = 0; i < existingCustomers.size(); i++) {
            Customer existingCustomer = existingCustomers.get(i);
            Customer foundCustomer = foundCustomers.get(i);

            assertEquals(existingCustomer.getId(), foundCustomer.getId());
            assertEquals(existingCustomer.getEmail(), foundCustomer.getEmail());
            assertEquals(existingCustomer.getPhone(), foundCustomer.getPhone());
        }
        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomersShouldReturnEmptyListWhenNoCustomersExist() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> foundCustomers = customerService.getAllCustomers();

        assertTrue(foundCustomers.isEmpty());
        verify(customerRepository).findAll();
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.getCustomerById METHOD ------------

    @Test
    void getCustomerByIdShouldReturnsMatchingCustomerIfExists() {
        Customer testCustomer = TestDataFactory.createDefaultCustomer();
        int customerId = testCustomer.getId();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        Customer foundCustomer = customerService.getCustomerById(customerId);

        assertEquals(testCustomer.getEmail(), foundCustomer.getEmail());
        assertEquals(testCustomer.getPhone(), foundCustomer.getPhone());
        assertEquals(testCustomer.getId(), foundCustomer.getId());
        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerByIdShouldThrowsExceptionIfNoMatchingCustomerExists() {
        int nonExistentCustomerId = 111;

        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.getCustomerById(nonExistentCustomerId)
        );

        assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
        verify(customerRepository).findById(nonExistentCustomerId);
    }

    //endregion

    //region ------------ TESTS FOR CustomerService.findOrCreateCustomer METHOD ------------

    @Test
    void findOrCreateCustomerShouldReturnsMatchingCustomerIfExists() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        Customer usedCustomer = CustomerTestBuilder.aCustomer()
            .withEmail(existingCustomer.getEmail())
            .build();

        when(customerRepository.findByEmail(existingCustomer.getEmail())).thenReturn(existingCustomer);

        Customer resultingCustomer = customerService.findOrCreateCustomer(usedCustomer);

        assertEquals(existingCustomer.getEmail(), resultingCustomer.getEmail());
        assertEquals(existingCustomer.getPhone(), resultingCustomer.getPhone());
        assertEquals(existingCustomer.getId(), resultingCustomer.getId());
        verify(customerRepository).findByEmail(usedCustomer.getEmail());
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void findOrCreateCustomerShouldCreateGivenCustomerIfNotExists() {
        Customer customerToCreate = new Customer();

        when(customerRepository.findByEmail(customerToCreate.getEmail())).thenReturn(null);
        when(customerRepository.save(customerToCreate)).thenReturn(customerToCreate);

        Customer foundCustomer = customerService.findOrCreateCustomer(customerToCreate);

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
        Customer nonExistentCustomer = TestDataFactory.createDefaultCustomer();
        int nonExistentCustomerId = 111;

        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.updateCustomer(nonExistentCustomerId, nonExistentCustomer)
        );

        assertEquals("No customer found with the ID : " + nonExistentCustomerId + ".", ex.getMessage());
        verify(customerRepository).findById(nonExistentCustomerId);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void updateCustomerShouldThrowExceptionIfEmailAlreadyUsedByAnotherCustomer() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = CustomerTestBuilder.aCustomer()
            .withId(id)
            .withEmail("duplicate@example.com")
            .withPhone("0600000000")
            .build();

        Customer otherCustomer = CustomerTestBuilder.aCustomer()
            .withId(999)
            .withEmail("duplicate@example.com")
            .build();

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
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int idInPath = 1;
        int idInBody = 2;

        Customer updatedCustomer = CustomerTestBuilder.aCustomer()
            .withId(idInBody)
            .withEmail(existingCustomer.getEmail())
            .withPhone(existingCustomer.getPhone())
            .build();

        when(customerRepository.findById(idInPath)).thenReturn(Optional.of(existingCustomer));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.updateCustomer(idInPath, updatedCustomer)
        );

        assertTrue(ex.getMessage().contains("Client ID mismatch"));
        verify(customerRepository).findById(idInPath);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void updateExistingCustomerShouldUpdatePhoneSuccessfullyIfEmailNotChanged() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = CustomerTestBuilder.aCustomer()
            .withId(id)
            .withEmail("john@example.com")
            .withPhone("0700000000")
            .build();

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
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int id = existingCustomer.getId();

        Customer updatedCustomer = CustomerTestBuilder.aCustomer()
            .withId(id)
            .withEmail("new.email@example.com")
            .withPhone("0600000000")
            .build();

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