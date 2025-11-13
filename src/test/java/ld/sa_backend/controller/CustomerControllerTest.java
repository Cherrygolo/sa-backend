package ld.sa_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.controller.advice.ApplicationControllerAdvice;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Customer testCustomer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Création du controller avec le service mocké
        CustomerController customerController = new CustomerController(customerService);

        // Configuration de MockMvc avec l’advice global pour les erreurs
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .build();

        // Client de test
        testCustomer = new Customer();
        testCustomer.setId(1);
        testCustomer.setEmail(testCustomer.getEmail());
    }

    //region ------------ TESTS FOR CustomerController.createCustomer ------------

    @Test
    void shouldCreateCustomerSucessfully() throws Exception {
        Mockito.when(customerService.createCustomer(any(Customer.class)))
                .thenReturn(testCustomer);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(testCustomer.getEmail()));
    }

    @Test
    void shouldReturnConflictErrorStatusWhenCreatingCustomerWithDuplicateEmail() throws Exception {
        Mockito.when(customerService.createCustomer(any(Customer.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Email already exists"));

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT_WITH_EXISTING_DATA"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void shouldPassCorrectEmailToService() throws Exception {
        Mockito.when(customerService.createCustomer(any(Customer.class)))
                .thenReturn(testCustomer);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "test@example.com"}
                        """))
                .andExpect(status().isCreated());

        verify(customerService).createCustomer(argThat(
            customer -> "test@example.com".equals(customer.getEmail())
        ));
    }

    //endregion

    //region ------------ TESTS FOR CustomerController.getAllCustomers ------------

    @Test
    void shouldReturnAllCustomers() throws Exception {
        Mockito.when(customerService.getAllCustomers())
                .thenReturn(List.of(testCustomer));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(testCustomer.getEmail()));
    }

    //endregion

    //region ------------ TESTS FOR CustomerController.getCustomerById ------------

    @Test
    void shouldReturnCustomerById() throws Exception {
        Mockito.when(customerService.getCustomerById(eq(1)))
                .thenReturn(testCustomer);

        mockMvc.perform(get("/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testCustomer.getEmail()));
    }

    @Test
    void shouldReturnNotFoundStatusWhenCustomerNotFound() throws Exception {
        Mockito.when(customerService.getCustomerById(eq(99)))
                .thenThrow(new EntityNotFoundException("No customer found with ID 99."));

        mockMvc.perform(get("/customer/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No customer found with ID 99."));
    }

    //endregion

    //region ------------ TESTS FOR CustomerController.updateCustomer ------------

    @Test
    void shouldUpdateCustomer() throws Exception {
        Mockito.when(customerService.updateCustomer(eq(1), any(Customer.class)))
                .thenReturn(testCustomer);

        mockMvc.perform(put("/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentCustomer() throws Exception {
        Mockito.when(customerService.updateCustomer(eq(999), any(Customer.class)))
                .thenThrow(new EntityNotFoundException("Customer not found"));

        mockMvc.perform(put("/customer/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    //endregion

    //region ------------ TESTS FOR CustomerController.deleteCustomer ------------

    @Test
    void shouldDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/customer/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(eq(1));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCustomer() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(eq(999));

        mockMvc.perform(delete("/customer/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    //endregion
}
