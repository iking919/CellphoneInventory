package CellphoneInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
    InventoryParserTest verifies that inventory files are parsed into phones and that invalid input
    follows the parser's intended skip or fail behavior
 */
class InventoryParserTest {

    // Initialize a temporary directory so parser tests use isolated files instead of changing the real inventory.
    @TempDir
    Path tempDir;

    // parsesValidRowsAndAllowsTrailingComma tests that a trailing 
    // comma is accepted for compatibility with the supplied inventory file.
    // Both the row of Google Pixel without trailing comma and Apple iPhone with trailing comma should become phone objects.
    @Test
    void parsesValidRowsAndAllowsTrailingComma() throws IOException {
        
        // Initialize small dataset for testing. Write both examples of phones into the file.
        Path inventory = tempDir.resolve("phones.txt");
        Files.write(inventory, List.of(
                "Google,Pixel 8,128 GB,Black,799.99",
                "Apple,iPhone 15,256 GB,Blue,929.99,"));

        // Parse the test file using Inventory Parser function.
        List<Phone> phones = InventoryParser.parseInventory(inventory.toString());

        // Ensure that both phones were created correctly by looking at phone list size, and elements of both phones.
        assertEquals(2, phones.size());
        assertEquals("Google", phones.get(0).getBrand());
        assertEquals("iPhone 15", phones.get(1).getModel());
        assertEquals(929.99, phones.get(1).getPrice());
    }

    // skipsInvalidRowsAndContinuesParsing tests that one invalid phone row does not prevent later valid rows from loading.
    @Test
    void skipsInvalidRowsAndContinuesParsing() throws IOException {

        // Initialize a dataset that includes separate failures (invalid price, extra field, and invalid storage).
        // Keep valid rows on both sides of those failures to verify the parser continues processing the file.
        Path inventory = tempDir.resolve("mixed-rows.txt");
        Files.write(inventory, List.of(
                "Google,Pixel 8,128 GB,Black,799.99",
                "Apple,iPhone 15,128 GB,Blue,not-a-price",
                "Samsung,Galaxy S24,128 GB,Gray,799.99,extra",
                "Motorola,Edge,128 MB,Black,499.99",
                "Samsung,Galaxy S24,256 GB,Gray,859.99"));

        // Parse the mixed dataset. Invalid rows should be reported and skipped rather than aborting the parse.
        List<Phone> phones = InventoryParser.parseInventory(inventory.toString());

        // Ensure only the 2 valid phones were added to the phone list.
        assertEquals(2, phones.size());

        // Ensure that the valid phones before and after the failures are both present, in their original file order.
        assertEquals(List.of("Google", "Samsung"),
                phones.stream().map(Phone::getBrand).toList());
    }

    // rejectsAnEmptyInventoryFile tests that a file without any rows is treated as an inventory loading error.
    @Test
    void rejectsAnEmptyInventoryFile() throws IOException {

        // Initialize an empty dataset to attempt to load.
        Path inventory = tempDir.resolve("empty.txt");
        Files.createFile(inventory);

        // Attempt to parse the empty file, ensuring an illegal argument exception is thrown.
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> InventoryParser.parseInventory(inventory.toString()));

        // Ensure the exception clearly identifies the reason the inventory could not be loaded.
        assertTrue(exception.getMessage().contains("file is empty"));
    }

    // propagatesFileReadErrors tests that file system errors are passed to the caller instead of being silently ignored.
    @Test
    void propagatesFileReadErrors() {

        // Initialize a path that does not exist in the temporary directory.
        Path missingInventory = tempDir.resolve("missing.txt");

        // Attempting to parse it should throw the specific missing file exception for Main to handle.
        assertThrows(NoSuchFileException.class,
                () -> InventoryParser.parseInventory(missingInventory.toString()));
    }
}
