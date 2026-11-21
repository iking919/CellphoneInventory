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

        // Create a Path object from the provided file path
        Path filePath = Path.of(dataFilePath);
        List<Phone> phones = new ArrayList<>();

        // Read all lines from the file into a list of strings
        List<String> lines = Files.readAllLines(filePath);

        // Check if the file is empty and throw an exception if it is
        if (lines.isEmpty()) {
            throw emptyFile(filePath, "file is empty");
        }

        // Iterate through each line in the file
        for (int i = 0; i < lines.size(); i++) {

            // Split the line into parts using a comma as the delimiter, allowing for empty fields
            String[] parts = lines.get(i).split(",", -1);
            int fieldCount = parts.length;

            // Handle the case where the last field (newline space) is empty, reducing the field count by one
            if (fieldCount == 6 && parts[5].strip().isEmpty()) {
                fieldCount--;
            }

            // Validate that the number of fields is exactly 5, throwing an exception if not
            if (fieldCount != 5) {
                throw invalidRow(filePath, i + 1, "invalid number of fields: expected 5, got " + fieldCount);
            }

            // Create an array to hold the stripped values of each field
            String[] values = new String[5];
            for (int field = 0; field < values.length; field++) {
                values[field] = parts[field].strip();

                // If any field is empty, throw an exception 
                if (values[field].isEmpty()) {
                    throw invalidRow(filePath, i + 1, "field " + (field + 1) + " is empty");
                }
            }

            // Attempt to parse the price field as a double, throwing an exception if it fails
            double price;
            try {
                price = Double.parseDouble(values[4]);
            } catch (NumberFormatException e) {
                throw invalidPrice(filePath, i + 1, "price is not a valid number: \"" + values[4] + "\"", e);
            }

            // Create a new Phone object with the parsed values and add it to the list of phones
            phones.add(new Phone(values[0], values[1], values[2], values[3], price));
        }
        return phones;
    }

    // Helper method to create an IllegalArgumentException for an empty file
    private static IllegalArgumentException emptyFile(Path filePath, String message) {
        return new IllegalArgumentException(filePath + ": " + message);
    }

    // Helper method to create an IllegalArgumentException for an invalid row
    private static IllegalArgumentException invalidRow(Path filePath, int lineNumber, String message) {
        return new IllegalArgumentException(filePath + ":" + lineNumber + ": " + message);
    }

    // Helper method to create an IllegalArgumentException for an invalid price, including the cause of the exception
    private static IllegalArgumentException invalidPrice( Path filePath, int lineNumber, String message, Exception cause) {
        return new IllegalArgumentException(filePath + ":" + lineNumber + ": " + message, cause);
    }
}

