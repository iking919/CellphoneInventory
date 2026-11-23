import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
    InventoryParser provides a static method to parse a text file containing phone inventory data, returning a list of Phone objects. 
    Each line in the file should contain five comma-separated fields: brand, model, storage, color, and price. 
    The parser validates the data and throws exceptions for any invalid rows.
*/
public class InventoryParser {

    // ParseInventory parses a text file containing phone inventory data and returns a list of Phone objects.
    public static List<Phone> parseInventory(String dataFilePath) throws IOException {

        // Read all lines from the specified file path.
        Path filePath = Path.of(dataFilePath);
        List<Phone> phones = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

        // Check if the file is empty and throw an exception if it is.
        if (lines.isEmpty()) {
            throw emptyFile(filePath, "file is empty");
        }

        // Iterate through each line in the file and parse it into a Phone object.
        for (int i = 0; i < lines.size(); i++) {
            try {

                // Split the line into parts using a comma as the delimiter.
                String[] parts = lines.get(i).split(",", -1);
                int fieldCount = parts.length;

                // Handle the case where there is an extra empty field at the end of the line.
                if (fieldCount == 6 && parts[5].strip().isEmpty()) {
                    fieldCount--;
                }

                // Validate that the line has exactly 5 fields and throw an exception if it does not.
                if (fieldCount != 5) {
                    throw invalidRow(filePath, i + 1, "invalid number of fields: expected 5, got " + fieldCount);
                }

                // Create an array to hold the values of the fields and validate that none of them are empty.
                String[] values = new String[5];
                for (int field = 0; field < values.length; field++) {
                    values[field] = parts[field].strip();

                    // Throw an exception if any of the fields are empty.
                    if (values[field].isEmpty()) {
                        throw invalidRow(filePath, i + 1, "field " + (field + 1) + " is empty");
                    }
                }

                // Parse the price field into a double and throw an exception if it is not a valid number.
                double price;
                try {
                    price = Double.parseDouble(values[4]);
                } catch (NumberFormatException e) {
                    throw invalidPrice(filePath, i + 1, "price is not a valid number: \"" + values[4] + "\"", e);
                }

                // Create a new Phone object using the parsed values and add it to the list of phones.
                phones.add(new Phone(values[0], values[1], values[2], values[3], price));
            } catch (IllegalArgumentException e) {

                // Print a warning message to the console for any invalid rows and continue processing the rest of the file.
                System.err.println("WARNING: Skipping invalid row: " + e.getMessage());
            }
        }

        // Return the list of valid Phone objects, after inventory has been parsed and validated.
        return phones;
    }

    // Helper methods to create IllegalArgumentException instances with detailed error messages.
    private static IllegalArgumentException emptyFile(Path filePath, String message) {
        return new IllegalArgumentException(filePath + ": " + message);
    }

    // Helper method to create an IllegalArgumentException for invalid rows with line number and message.
    private static IllegalArgumentException invalidRow(Path filePath, int lineNumber, String message) {
        return new IllegalArgumentException(filePath + ":" + lineNumber + ": " + message);
    }

    // Helper method to create an IllegalArgumentException for invalid price with line number, message, and cause.
    private static IllegalArgumentException invalidPrice(
            Path filePath, int lineNumber, String message, Exception cause) {
        return new IllegalArgumentException(filePath + ":" + lineNumber + ": " + message, cause);
    }
}
