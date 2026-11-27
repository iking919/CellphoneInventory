package CellphoneInventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// PhoneInventoryServiceTest verifies phone loading, filtering, adding, deleting,
// and updating inventory records in temporary test files.
class PhoneInventoryServiceTest {

    // Initialize a temporary directory so inventory tests do not modify the application's data file.
    @TempDir
    Path tempDir;

    // searchesParsedInventoryUsingFilters tests that a parsed inventory can be filtered by phone brand.
    @Test
    void searchesParsedInventoryUsingFilters() throws IOException {

        // Initialize two inventory rows and filter the parsed phones by Samsung.
        Path inventoryFile = tempDir.resolve("phones.txt");
        Files.writeString(inventoryFile, String.join(System.lineSeparator(),
                "Samsung,Galaxy S,128GB,Black,799.99",
                "Google,Pixel 8,256GB,Gray,699.99"));
        PhoneInventoryService service = new PhoneInventoryService(inventoryFile.toString());

        List<Phone> results = service.searchPhones(List.of("brand => Samsung"));

        // Ensure only the Samsung phone is returned with the expected model.
        assertEquals(1, results.size());
        assertEquals("Galaxy S", results.get(0).getModel());
    }

    // rejectsInvalidSearchFilters tests that an empty filter value is rejected.
    @Test
    void rejectsInvalidSearchFilters() {

        // A brand filter with no value should throw an invalid-filter exception.
        PhoneInventoryService service = new PhoneInventoryService("unused.txt");

        assertThrows(PhoneInventoryService.InvalidFiltersException.class,
                () -> service.searchPhones(List.of("brand => ")));
    }

    // appendsPhoneAsCsvWithoutNeedingAnExistingFinalNewline tests adding to a file that ends without a newline.
    @Test
    void appendsPhoneAsCsvWithoutNeedingAnExistingFinalNewline() throws IOException {

        // Initialize an inventory file and append a new Samsung phone record.
        Path inventoryFile = tempDir.resolve("phones.txt");
        Files.writeString(inventoryFile, "Google,Pixel 8,128 GB,Gray,699.99");
        PhoneInventoryService service = new PhoneInventoryService(inventoryFile.toString());

        Phone added = service.addPhone(new Phone("Samsung", "Galaxy S", "128 GB", "Black", 799.99));

        // Ensure a separator was added and both inventory rows were written correctly.
        assertEquals(List.of(
                "Google,Pixel 8,128 GB,Gray,699.99",
                "Samsung,Galaxy S,128 GB,Black,799.99"),
                Files.readAllLines(inventoryFile));
        assertEquals(2, service.getPhones().size());
        assertEquals("Galaxy S", added.getModel());
    }

    // deletesOneMatchingPhoneAndPreservesOtherRows tests that only the first matching row is deleted.
    @Test
    void deletesOneMatchingPhoneAndPreservesOtherRows() throws IOException {

        // Initialize matching and malformed rows to verify that unmatched data is preserved.
        Path inventoryFile = tempDir.resolve("phones.txt");
        String matchingRow = "Samsung,Galaxy S,128 GB,Black,799.99,";
        String malformedRow = "not a valid inventory row";
        Files.write(inventoryFile, List.of(matchingRow, malformedRow, matchingRow));
        PhoneInventoryService service = new PhoneInventoryService(inventoryFile.toString());

        assertTrue(service.deletePhone(new Phone("Samsung", "Galaxy S", "128 GB", "Black", 799.99)));

        // Ensure the first match was removed while the malformed row and duplicate match remain.
        assertEquals(List.of(malformedRow, matchingRow), Files.readAllLines(inventoryFile));
    }

    // deletingLastPhoneLeavesAValidEmptyInventory tests deleting the final inventory record.
    @Test
    void deletingLastPhoneLeavesAValidEmptyInventory() throws IOException {

        // Initialize a file containing only one phone.
        Path inventoryFile = tempDir.resolve("phones.txt");
        Files.writeString(inventoryFile, "Samsung,Galaxy S,128 GB,Black,799.99");
        PhoneInventoryService service = new PhoneInventoryService(inventoryFile.toString());

        assertTrue(service.deletePhone(new Phone("Samsung", "Galaxy S", "128 GB", "Black", 799.99)));

        // Ensure the file is empty and the inventory parser returns no phones.
        assertTrue(Files.readAllLines(inventoryFile).isEmpty());
        assertTrue(service.getPhones().isEmpty());
    }

    // updatesMatchingPhoneAndPreservesOtherRows tests replacing the first matching inventory record.
    @Test
    void updatesMatchingPhoneAndPreservesOtherRows() throws IOException {

        // Initialize an inventory containing a phone to update and another phone to preserve.
        Path inventoryFile = tempDir.resolve("phones.txt");
        Files.write(inventoryFile, List.of(
                "Google,Pixel 8,128 GB,Gray,699.99,",
                "Samsung,Galaxy S,128 GB,Black,799.99"));
        PhoneInventoryService service = new PhoneInventoryService(inventoryFile.toString());

        assertTrue(service.updatePhone(
                new Phone("Google", "Pixel 8", "128 GB", "Gray", 699.99),
                new Phone("Google", "Pixel 8 Pro", "256 GB", "Black", 999.99)));

        // Ensure the original phone was replaced and the unrelated phone row remained unchanged.
        assertEquals(List.of(
                "Google,Pixel 8 Pro,256 GB,Black,999.99",
                "Samsung,Galaxy S,128 GB,Black,799.99"),
                Files.readAllLines(inventoryFile));
    }
}
