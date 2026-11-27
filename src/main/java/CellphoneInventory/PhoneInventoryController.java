package CellphoneInventory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
    PhoneInventoryController provides a REST controller for listing, adding, updating, 
    deleting, and searching phones in the inventory.
*/

@RestController
public class PhoneInventoryController {

    // Hold an attribute of the supplied PhoneInventoryService class, which will read and modify the inventory.
    private final PhoneInventoryService inventoryService;

    // PhoneInventory Controller initialization takes in and stores inventoryService.
    public PhoneInventoryController(PhoneInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // getPhones method returns all phones currently stored in the inventory. Throws an exception if
    // the inventory cannot be loaded. (GET method for reading)
    @GetMapping("/api/phones")
    public List<Phone> getPhones() {
        try {
            return inventoryService.getPhones();
        // If an exception is thrown by the inventory service, throw HttpStatus internal error accordingly.
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load inventory data",
                    e);
        }
    }

    // addPhone method takes a PhoneWriteRequest and adds phone to inventory returning the phone or
    // throwing exception if phone could not be saved. (POST method for creating)
    @PostMapping("/api/phones")
    @ResponseStatus(HttpStatus.CREATED)
    public Phone addPhone(@RequestBody PhoneWriteRequest request) {
        Phone phone = toPhone(request);
        try {
            return inventoryService.addPhone(phone);
        // If an exception is thrown by the inventoryService, throw HttpStatus internal error accordingly.
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save phone to inventory",
                    e);
        }
    }

    // updatePhone method takes an update object containing the original and updated phone info,
    // then replaces the original phone with the updated phone. (PUT method for updating)
    @PutMapping("/api/phones")
    public ResponseEntity<Void> updatePhone(@RequestBody Map<String, PhoneWriteRequest> request) {
        // Ensure the update request isn't empty.
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone update data is required");
        }

        // Cast requests to phones.
        Phone originalPhone = toPhone(request.get("original"));
        Phone updatedPhone = toPhone(request.get("updated"));

        // Attempt to update phones, throwing errors if phone wasn't found or update failed.
        try {
            if (!inventoryService.updatePhone(originalPhone, updatedPhone)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone was not found in inventory");
            }
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not update inventory file",
                    e);
        }
    }


    // deletePhone method takes a PhoneWriteRequest and deletes the corresponding phone from the inventory.
    // (DELETE method for deleting)
    @DeleteMapping("/api/phones")
    public ResponseEntity<Void> deletePhone(@RequestBody PhoneWriteRequest request) {
        Phone phone = toPhone(request);

        // Attempt to delete phone throwing errors if phone wasn't found in inventory or if updating file fails.
        try {
            if (!inventoryService.deletePhone(phone)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone was not found in inventory");
            }
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not update inventory file",
                    e);
        }
    }

    /**
     * Searches the inventory using the filters supplied in the request body.
     *
     * @param request filter values to apply to the inventory
     * @return phones that match the supplied filters
     * @throws ResponseStatusException if the filters are invalid or inventory data
     *                                cannot be loaded
     */

    // searchPhones method searches inventory of phones with a request, returning a list of phones 
    // that fit the requested filters.
    @PostMapping("/api/phones/search")
    public List<Phone> searchPhones(@RequestBody List<String> filters) {

        // Attempt to search phones, returning errors if request is incorrect or 
        // if inventory couldn't be loaded.
        try {
            return inventoryService.searchPhones(filters);
        } catch (PhoneInventoryService.InvalidFiltersException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load inventory data",
                    e);
        }
    }


    // toPhone method takes an HTTP PhoneWriteRequest and converts it it into a Phone object,
    // reporting HTTP bad-requets if request is invalid or missing.
    private static Phone toPhone(PhoneWriteRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone data is required");
        }

        try {
            return request.toPhone();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}