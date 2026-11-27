package CellphoneInventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    PhoneInventoryControllerTest verifies the inventory REST endpoints using MockMvc.
    The controller is tested independently from file storage by mocking the inventory service.
*/
@WebMvcTest(PhoneInventoryController.class)
class PhoneInventoryControllerTest {

    // MockMvc sends HTTP requests to the controller without starting a separate web server.
    @Autowired
    private MockMvc mockMvc;

    // Mock the inventory service so endpoint tests control service behavior.
    @MockitoBean
    private PhoneInventoryService inventoryService;

    // postsPhone tests that a valid POST request adds a phone and returns HTTP 201.
    @Test
    void postsPhone() throws Exception {

        // Initialize a phone and configure the service to return it after addition.
        Phone phone = new Phone("Google", "Pixel 8", "128 GB", "Gray", 699.99);
        when(inventoryService.addPhone(any(Phone.class))).thenReturn(phone);

        // Send a POST request and ensure the created phone is returned.
        mockMvc.perform(post("/api/phones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brand": "Google",
                                  "model": "Pixel 8",
                                  "storage": "128 GB",
                                  "color": "Gray",
                                  "price": 699.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Google"))
                .andExpect(jsonPath("$.model").value("Pixel 8"));

        // Verify that the service add operation was called.
        verify(inventoryService).addPhone(any(Phone.class));
    }

    // putsPhone tests that a valid PUT request updates a phone and returns HTTP 204.
    @Test
    void putsPhone() throws Exception {

        // Configure the service to confirm that the phone update succeeded.
        when(inventoryService.updatePhone(any(Phone.class), any(Phone.class))).thenReturn(true);

        // Send the original and updated phones in a PUT request.
        mockMvc.perform(put("/api/phones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "original": {
                                    "brand": "Google",
                                    "model": "Pixel 8",
                                    "storage": "128 GB",
                                    "color": "Gray",
                                    "price": 699.99
                                  },
                                  "updated": {
                                    "brand": "Google",
                                    "model": "Pixel 8 Pro",
                                    "storage": "256 GB",
                                    "color": "Black",
                                    "price": 999.99
                                  }
                                }
                                """))
                .andExpect(status().isNoContent());

        // Verify that the service update operation was called.
        verify(inventoryService).updatePhone(any(Phone.class), any(Phone.class));
    }

    // deletesPhone tests that a valid DELETE request removes a phone and returns HTTP 204.
    @Test
    void deletesPhone() throws Exception {

        // Configure the service to confirm that the phone deletion succeeded.
        when(inventoryService.deletePhone(any(Phone.class))).thenReturn(true);

        // Send a DELETE request containing the phone to remove.
        mockMvc.perform(delete("/api/phones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brand": "Google",
                                  "model": "Pixel 8",
                                  "storage": "128 GB",
                                  "color": "Gray",
                                  "price": 699.99
                                }
                                """))
                .andExpect(status().isNoContent());

        // Verify that the service delete operation was called.
        verify(inventoryService).deletePhone(any(Phone.class));
    }

    // searchesPhones tests that a filter list is passed to the service and matching results are returned.
    @Test
    void searchesPhones() throws Exception {

        // Initialize one matching phone and configure the search service result.
        Phone phone = new Phone("Samsung", "Galaxy S", "128 GB", "Black", 799.99);
        when(inventoryService.searchPhones(List.of("brand => Samsung"))).thenReturn(List.of(phone));

        // Send a JSON filter array to the search endpoint.
        mockMvc.perform(post("/api/phones/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                ["brand => Samsung"]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].model").value("Galaxy S"));

        // Verify that the service received the supplied filter list.
        verify(inventoryService).searchPhones(List.of("brand => Samsung"));
    }
}
