package ld.sa_backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.repository.CustomerRepository;
import ld.sa_backend.testutils.CustomerTestBuilder;
import ld.sa_backend.testutils.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    //region ------------ CREATE CUSTOMER ------------

    @Test
    void createCustomer_shouldSaveCustomer_whenEmailIsNotUsed() {
        Customer customerToCreate = TestDataFactory.createDefaultCustomer();

        when(customerRepository.findByEmail(customerToCreate.getEmail())).thenReturn(null);
        when(customerRepository.save(customerToCreate)).thenReturn(customerToCreate);

        Customer createdCustomer = customerService.createCustomer(customerToCreate);

        assertEquals(customerToCreate.getEmail(), createdCustomer.getEmail());
        assertEquals(customerToCreate.getPhone(), createdCustomer.getPhone());
        assertEquals(customerToCreate.getId(), createdCustomer.getId());

        verify(customerRepository).findByEmail(customerToCreate.getEmail());
        verify(customerRepository).save(customerToCreate);
    }

    @Test
    void createCustomer_shouldThrowException_whenEmailAlreadyExists() {
        Customer customerToCreate = TestDataFactory.createDefaultCustomer();

        when(customerRepository.findByEmail(customerToCreate.getEmail()))
            .thenReturn(customerToCreate);

        DataIntegrityViolationException exception = assertThrows(
            DataIntegrityViolationException.class,
            () -> customerService.createCustomer(customerToCreate)
        );

        assertEquals(
            "An user already exists with the email address : " + customerToCreate.getEmail(),
            exception.getMessage()
        );

        verify(customerRepository).findByEmail(customerToCreate.getEmail());
        verify(customerRepository, never()).save(any());
    }

    //endregion

    //region ------------ DELETE CUSTOMER ------------

    @Test
    void deleteCustomer_shouldDeleteCustomer_whenCustomerExists() {
        int customerId = 1;

        when(customerRepository.existsById(customerId)).thenReturn(true);

        customerService.deleteCustomer(customerId);

        verify(customerRepository).existsById(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_shouldThrowException_whenCustomerDoesNotExist() {
        int nonExistentCustomerId = 999;

        when(customerRepository.existsById(nonExistentCustomerId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.deleteCustomer(nonExistentCustomerId)
        );

        assertEquals(
            "No customer found with the ID : " + nonExistentCustomerId + ".",
            exception.getMessage()
        );

        verify(customerRepository).existsById(nonExistentCustomerId);
        verify(customerRepository, never()).deleteById(anyInt());
    }

    //endregion

    //region ------------ GET ALL CUSTOMERS ------------

    @Test
    void getAllCustomers_shouldReturnAllCustomers_whenCustomersExist() {
        List<Customer> existingCustomers = TestDataFactory.createCustomerList(2);

        when(customerRepository.findAll()).thenReturn(existingCustomers);

        List<Customer> returnedCustomers = customerService.getAllCustomers();

        assertEquals(existingCustomers.size(), returnedCustomers.size());
        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomers_shouldReturnEmptyList_whenNoCustomerExists() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> returnedCustomers = customerService.getAllCustomers();

        assertTrue(returnedCustomers.isEmpty());
        verify(customerRepository).findAll();
    }

    //endregion

    //region ------------ GET CUSTOMER BY ID ------------

    @Test
    void getCustomerById_shouldReturnCustomer_whenCustomerExists() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int customerId = existingCustomer.getId();

        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(existingCustomer));

        Customer foundCustomer = customerService.getCustomerById(customerId);

        assertEquals(existingCustomer.getId(), foundCustomer.getId());
        assertEquals(existingCustomer.getEmail(), foundCustomer.getEmail());
        assertEquals(existingCustomer.getPhone(), foundCustomer.getPhone());

        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerById_shouldThrowException_whenCustomerDoesNotExist() {
        int nonExistentCustomerId = 111;

        when(customerRepository.findById(nonExistentCustomerId))
            .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.getCustomerById(nonExistentCustomerId)
        );

        assertEquals(
            "No customer found with the ID : " + nonExistentCustomerId + ".",
            exception.getMessage()
        );

        verify(customerRepository).findById(nonExistentCustomerId);
    }

    //endregion

    //region ------------ FIND OR CREATE CUSTOMER ------------

    @Test
    void findOrCreateCustomer_shouldReturnExistingCustomer_whenEmailExists() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        Customer inputCustomer = CustomerTestBuilder.aCustomer()
            .withEmail(existingCustomer.getEmail())
            .build();

        when(customerRepository.findByEmail(existingCustomer.getEmail()))
            .thenReturn(existingCustomer);

        Customer foundCustomer = customerService.findOrCreateCustomer(inputCustomer);

        assertEquals(existingCustomer.getId(), foundCustomer.getId());
        verify(customerRepository).findByEmail(existingCustomer.getEmail());
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void findOrCreateCustomer_shouldCreateCustomer_whenCustomerDoesNotExist() {
        Customer customerToCreate = CustomerTestBuilder.aCustomer().build();

        when(customerRepository.findByEmail(customerToCreate.getEmail()))
            .thenReturn(null);
        when(customerRepository.save(customerToCreate))
            .thenReturn(customerToCreate);

        Customer createdCustomer = customerService.findOrCreateCustomer(customerToCreate);

        assertEquals(customerToCreate.getEmail(), createdCustomer.getEmail());
        verify(customerRepository).findByEmail(customerToCreate.getEmail());
        verify(customerRepository).save(customerToCreate);
    }

    //endregion

    //region ------------ UPDATE CUSTOMER ------------

    @Test
    void updateCustomer_shouldThrowException_whenCustomerDoesNotExist() {
        Customer customerToUpdate = TestDataFactory.createDefaultCustomer();
        int customerId = 999;

        when(customerRepository.findById(customerId))
            .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> customerService.updateCustomer(customerId, customerToUpdate)
        );

        assertEquals(
            "No customer found with the ID : " + customerId + ".",
            exception.getMessage()
        );

        verify(customerRepository).findById(customerId);
    }

    @Test
    void updateCustomer_shouldUpdateCustomer_whenEmailIsAvailable() {
        Customer existingCustomer = TestDataFactory.createDefaultCustomer();
        int customerId = existingCustomer.getId();

        Customer customerToUpdate = CustomerTestBuilder.aCustomer()
            .withId(customerId)
            .withEmail("new.email@example.com")
            .withPhone("0700000000")
            .build();

        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(customerToUpdate.getEmail()))
            .thenReturn(null);
        when(customerRepository.save(any(Customer.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Customer updatedCustomer = customerService.updateCustomer(customerId, customerToUpdate);

        assertEquals("new.email@example.com", updatedCustomer.getEmail());
        assertEquals("0700000000", updatedCustomer.getPhone());

        verify(customerRepository).save(existingCustomer);
    }

    //endregion
}
