package CellphoneInventory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// MainIntegrationTest verifies user visible filtering and inventory loading behavior using controlled input and files.
class MainIntegrationTest {

    // Initialize a temporary directory for inventory files used by error-path integration tests.
    @TempDir
    Path tempDir;

    // acceptsFiltersConfirmsResultsAndExits tests the command line flow for adding criteria and displaying results.
    @Test
    void acceptsFiltersConfirmsResultsAndExits() {

        // Initialize input with a brand filter, a strict price filter, confirmation, and the exit command.
        // Define user input interactions.
        String input = "Brand => Google\nPrice => < $900\nconfirm\nexit\n";

        // Run the program with the scripted commands and capture everything printed to standard output.
        String result = captureApplicationInput(input);

        // Ensure the application confirms the criteria and displays representative matches in the results.
        // Also verify that successful matches do not trigger the no-results message.
        assertTrue(result.contains("Filters confirmed."));
        assertTrue(result.contains("Google Pixel 6a, 256 GB, Mineral Gray, $399.99"));
        assertTrue(result.contains("Google Pixel 7a, 256 GB, Mineral Gray, $549.99"));
        assertTrue(result.contains("Google Pixel 8, 128 GB, Hazel Gray, $799.99"));
        assertFalse(result.contains("No phones match the current filter criteria."));
    }

    // reportsInventoryReadErrors tests Main's handling of an unreadable inventory path.
    @Test
    void reportsInventoryReadErrors() {

        // Initialize a path that does not exist in the test directory.
        String result = captureMainOutput(tempDir.resolve("missing.txt"));

        // Run Main and ensure it reports the read error instead of starting the command loop.
        assertTrue(result.contains("ERROR: Could not read the inventory file:"));
    }

    // reportsWhenInventoryIsEmpty tests Main's response to an empty inventory file.
    @Test
    void reportsWhenInventoryIsEmpty() throws Exception {

        // Initialize an existing empty file.
        Path emptyInventory = tempDir.resolve("empty.txt");
        Files.createFile(emptyInventory);

        // Run Main and ensure it reports that no phones are available.
        String result = captureMainOutput(emptyInventory);

        assertTrue(result.contains("No valid phones found in the inventory file."));
    }

    // reportsWhenNoValidPhonesRemain tests the startup message when parsing skips every row.
    @Test
    void reportsWhenNoValidPhonesRemain() throws Exception {

        // Initialize a non-empty inventory containing one malformed row, which the parser will skip.
        Path invalidInventory = tempDir.resolve("invalid.txt");
        Files.write(invalidInventory, List.of("not a valid phone row"));

        // Run Main and ensure the empty-inventory path is reported.
        String result = captureMainOutput(invalidInventory);

        assertTrue(result.contains("No valid phones found in the inventory file."));
    }

    // rejectsInvalidFilterInput tests that invalid user criteria are rejected without being stored.
    @Test
    void rejectsInvalidFilterInput() {

        // Initialize an invalid brand filter followed by confirmation and exit commands.
        String result = captureApplicationInput("Brand =>\nconfirm\nexit\n");

        // Ensure validation reports the malformed input and leaves the current filter list empty.
        assertTrue(result.contains("Invalid filter format"));
        assertTrue(result.contains("Current filters: None"));
    }

    // captureApplicationInput method takes in an input string and runs the application as if a user had
    // typed the specified inputs, returning everything the applciation prints as a string.
    private static String captureApplicationInput(String input) {

        // Save the current process-wide input and output streams so the JUnit runner's streams can be restored
        // after Main finishes. The output buffer stores the text outputted by the application during this run.
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Create a UTF-8 output stream that writes into the buffer, replacing standard input with the
        // supplied commands and standard output with the capturing stream. Run Main.main so this test
        // exercises the same startup path used by the real application.
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOut);

            Main.main(new String[0]);
        } finally {

            // Main closes its Scanner, which also closes the substituted input stream. Restore the original
            // streams in a finally block so tests and JUnit output remain usable even if Main fails.
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        // Return the captured output as a string.
        return output.toString(StandardCharsets.UTF_8);
    }


    // captureMainOutput method takes in an inventoryPath file path and captures output and errors to test
    // inventory loading and exception reporting
    private static String captureMainOutput(Path inventoryPath) {

        // Save all process-wide streams before redirecting them. Keep stdout and stderr in separate buffers so
        // the application can continue to use the appropriate channel while the test captures both.
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        // Create UTF-8 capture streams, with empty inputs and redirect both output channels. 
        // Main.run accepts the temporary path directly, allowing each test
        // to exercise a missing, empty, or invalid inventory without changing the original hardcoded text file path.
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr = new PrintStream(error, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(new byte[0]));
            System.setOut(capturedOut);
            System.setErr(capturedErr);

            Main.run(inventoryPath.toString());
        } finally {
            // Restore every replaced process-wide stream even if parsing or application startup throws, so
            // this helper cannot leave the JUnit runner or any subsequent tests with redirected output.
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        // Return captured output and errors as a combined string.
        return output.toString(StandardCharsets.UTF_8) + error.toString(StandardCharsets.UTF_8);
    }
}
