package ld.sa_backend.unit.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import ld.sa_backend.controller.CustomerController;
import ld.sa_backend.controller.advice.ApplicationControllerAdvice;
import ld.sa_backend.entity.Customer;
import ld.sa_backend.service.CustomerService;
import ld.sa_backend.testutils.CustomerTestBuilder;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .build();
    }

    //region ---------- CREATE CUSTOMER ----------

    @Test
    void createCustomer_shouldReturn201_whenCustomerIsValid() throws Exception {
        Customer customer = CustomerTestBuilder.aCustomer().build();

        when(customerService.createCustomer(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void createCustomer_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        Customer customer = CustomerTestBuilder.aCustomer().build();

        when(customerService.createCustomer(any(Customer.class)))
                .thenThrow(new DataIntegrityViolationException("Email already exists"));

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT_WITH_EXISTING_DATA"));
    }

    //endregion

    //region ---------- GET ALL CUSTOMERS ----------

    @Test
    void getAllCustomers_shouldReturnListOfCustomers() throws Exception {
        Customer customer = CustomerTestBuilder.aCustomer().build();

        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(customer.getEmail()));
    }

    //endregion

    //region ---------- GET CUSTOMER BY ID ----------

    @Test
    void getCustomerById_shouldReturnCustomer_whenExists() throws Exception {
        Customer customer = CustomerTestBuilder.aCustomer().withId(1).build();

        when(customerService.getCustomerById(1)).thenReturn(customer);

        mockMvc.perform(get("/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void getCustomerById_shouldReturn404_whenCustomerDoesNotExist() throws Exception {
        when(customerService.getCustomerById(99))
                .thenThrow(new EntityNotFoundException("Customer not found"));

        mockMvc.perform(get("/customer/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"));
    }

    //endregion

    //region ---------- UPDATE CUSTOMER ----------

    @Test
    void updateCustomer_shouldReturnUpdatedCustomer() throws Exception {
        Customer customer = CustomerTestBuilder.aCustomer().withId(1).build();

        when(customerService.updateCustomer(eq(1), any(Customer.class)))
                .thenReturn(customer);

        mockMvc.perform(put("/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    //endregion

    //region ---------- DELETE CUSTOMER ----------

    @Test
    void deleteCustomer_shouldReturn204_whenCustomerExists() throws Exception {
        mockMvc.perform(delete("/customer/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1);
    }

    @Test
    void deleteCustomer_shouldReturn404_whenCustomerDoesNotExist() throws Exception {
        doThrow(new EntityNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(99);

        mockMvc.perform(delete("/customer/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"));
    }

    //endregion
}
