/**
 * CustomerControllerIT - Tests d'intégration pour le CustomerController.
 *
 * Vérifie le comportement des endpoints REST liés aux opérations sur les clients,
 * incluant la création, la mise à jour, la récupération et la suppression.
 * Les tests s'exécutent dans un environnement complet avec base de données et
 * dépendances simulées pour refléter les interactions réelles.
 */

package ld.sa_backend.it.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import ld.sa_backend.entity.Customer;
import ld.sa_backend.repository.CustomerRepository;
import ld.sa_backend.testutils.CustomerTestBuilder;

/**
 * Integration tests for {@link ld.sa_backend.controller.CustomerController}.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer persistedCustomer;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();

        // Création d’un customer via le Builder pour respecter le constructeur protégé
        persistedCustomer = customerRepository.save(
            CustomerTestBuilder.aCustomer()
                .withEmail("integration@test.com")
                .withPhone("0600000000")
                .build()
        );
    }

    //region ------------ CREATE CUSTOMER ------------

    @Test
    void createCustomer_shouldPersistCustomer_whenEmailIsUnique() throws Exception {
        Customer customerToCreate = CustomerTestBuilder.aCustomer()
            .withEmail("newcustomer@test.com")
            .withPhone("0611111111")
            .build();

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("newcustomer@test.com"));
    }

    @Test
    void createCustomer_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        Customer customerToCreate = CustomerTestBuilder.aCustomer()
            .withEmail(persistedCustomer.getEmail()) // même email que celui déjà présent
            .withPhone("0611111111")
            .build();

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToCreate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT_WITH_EXISTING_DATA"));
    }

    @Test
    void createCustomer_shouldReturnRequestBodyInvalid_whenJsonMalformed() throws Exception {
        String invalidJson = "{ \"email\": \"example@test.com\" "; // JSON mal formé

        mockMvc.perform(post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUEST_BODY_INVALID"));
    }

    //endregion

    //region ------------ GET CUSTOMER ------------

    @Test
    void getCustomerById_shouldReturnCustomer_whenCustomerExists() throws Exception {
        mockMvc.perform(get("/customer/{id}", persistedCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(persistedCustomer.getEmail()));
    }

    //endregion

    //region ------------ UPDATE CUSTOMER ------------

    @Test
    void updateCustomer_shouldUpdateCustomer_whenCustomerExists() throws Exception {
        Integer customerId = persistedCustomer.getId();

        Customer customerToUpdate = CustomerTestBuilder.aCustomer()
            .withId(customerId) // même ID que celui du client existant
            .withEmail("updated@test.com")
            .withPhone("0622222222")
            .build();

        mockMvc.perform(put("/customer/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    void updateCustomer_shouldReturnArgumentsInvalid_whenIdMismatch() throws Exception {
        Customer customerToUpdate = CustomerTestBuilder.aCustomer()
            .withId(persistedCustomer.getId() + 1)
            .withEmail("new@test.com")
            .build();

        mockMvc.perform(put("/customer/{id}", persistedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToUpdate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ARGUMENTS_INVALID"));
    }

    //region ------------ DELETE CUSTOMER ------------

    @Test
    void deleteCustomer_shouldRemoveCustomer_whenCustomerExists() throws Exception {
        mockMvc.perform(delete("/customer/{id}", persistedCustomer.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/customer/{id}", persistedCustomer.getId()))
                .andExpect(status().isNotFound());
    }

    //endregion
}
