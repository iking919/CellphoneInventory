package CellphoneInventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/*
    PhoneInventoryService provides inventory operations that read and update
    the phone data file. Mutating operations are synchronized to prevent
    concurrent updates from overwriting each other.
*/

@Service
public class PhoneInventoryService {

    // Store the path to the inventory file used by this service.
    private final String inventoryFile;

    // PhoneInventoryService initialization stores the configured inventory file path.
    public PhoneInventoryService(
            @Value("${inventory.file:data/cellphonedata.txt}") String inventoryFile) {
        this.inventoryFile = inventoryFile;
    }

    // getPhones method loads and returns every phone from the inventory file.
    // An exception is thrown if the inventory file cannot be read.
    public List<Phone> getPhones() throws IOException {
        return InventoryParser.parseInventory(inventoryFile);
    }

    // searchPhones method validates the supplied filters and returns phones that match them.
    // This method throws an InvalidFiltersException for a missing or invalid filter list.
    public List<Phone> searchPhones(List<String> filters) throws IOException {
        if (filters == null) {
            throw new InvalidFiltersException("Filters must be provided as a list.");
        }

        for (String filter : filters) {
            if (!InventoryService.validFilter(filter)) {
                throw new InvalidFiltersException("Invalid filter: " + filter);
            }
        }

        return InventoryService.filterPhones(getPhones(), filters);
    }

    // addPhone method appends a phone to the inventory file and returns the added phone.
    // This method is synchronized so inventory writes do not overlap.
    public synchronized Phone addPhone(Phone phone) throws IOException {

        // Specify inventoryFile path, getting existing contents.
        Path path = Path.of(inventoryFile);
        String existingContents = Files.exists(path)
                ? Files.readString(path, StandardCharsets.UTF_8)
                : "";

        // Add a line separator only when the file does not already end with a line break.
        String separator = !existingContents.isEmpty()
                && !existingContents.endsWith("\n")
                && !existingContents.endsWith("\r")
                ? System.lineSeparator()
                : "";

        // Add phone string to inventory file, creating the file if it does not 
        // exist or appending it to the end of the file.
        Files.writeString(
                path,
                separator + toInventoryRecord(phone) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        
        // Return the successfully added phone.
        return phone;
    }

    // deletePhone method deletes the first inventory record that exactly matches the supplied phone.
    // It returns true if a match is deleted or false if no match exists.
    public synchronized boolean deletePhone(Phone phone) throws IOException {

        // Specify inventoryFile path, getting existing contents as a list.
        Path path = Path.of(inventoryFile);
        List<String> originalLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String> remainingLines = new ArrayList<>(originalLines);

        // Search each inventory line and remove the first exact match.
        for (int i = 0; i < originalLines.size(); i++) {
            
            // If inventory line matches specified phone deletion, remove from 
            // remaining lines list and replace inventory file, returning true.
            if (matchesPhone(originalLines.get(i), phone)) {
                remainingLines.remove(i);
                replaceInventoryFile(path, remainingLines);
                return true;
            }
        }

        // Return false if no phone was found and deleted.
        return false;
    }

    // updatePhone method replaces the first inventory record that exactly matches the original phone.
    // It returns true if a match is updated or false if no match exists.
    public synchronized boolean updatePhone(Phone originalPhone, Phone updatedPhone) throws IOException {
        
        // Specify inventoryFile path, getting existing contents as a list.
        Path path = Path.of(inventoryFile);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        // Search each inventory line and replace the first exact match.
        for (int i = 0; i < lines.size(); i++) {

            // If inventory line matches the original phone, replace the inventory line 
            // with updated phone object, returning true.
            if (matchesPhone(lines.get(i), originalPhone)) {
                lines.set(i, toInventoryRecord(updatedPhone));
                replaceInventoryFile(path, lines);
                return true;
            }
        }

        // If no phone was found matching the original phone no edit is made, return false.
        return false;
    }

    // toInventoryRecord method formats a phone using the comma-separated inventory file format.
    private static String toInventoryRecord(Phone phone) {
        return String.join(",",
                phone.getBrand(),
                phone.getModel(),
                phone.getStorage(),
                phone.getColor(),
                Double.toString(phone.getPrice()));
    }

    // matchesPhone method checks whether a file line exactly represents the supplied phone.
    // Invalid lines and parsing failures are treated as non-matches.
    private static boolean matchesPhone(String line, Phone phone) {

        // Split along ",".
        String[] fields = line.split(",", -1);

        // Accont for trailing empty fields in inventory.
        if (fields.length == 6 && fields[5].strip().isEmpty()) {
            fields = java.util.Arrays.copyOf(fields, 5);
        }

        // If line doesn't contain exactly 5 fields return false.
        if (fields.length != 5) {
            return false;
        }

        // Attempt to create a phone using the current inventory line.
        try {
            Phone existing = new Phone(
                    fields[0].strip(),
                    fields[1].strip(),
                    fields[2].strip(),
                    fields[3].strip(),
                    Double.parseDouble(fields[4].strip()));
            
            // Return boolean indicating if current inventory line's phone object matches the
            // target phone.
            return existing.getBrand().equals(phone.getBrand())
                    && existing.getModel().equals(phone.getModel())
                    && existing.getStorage().equals(phone.getStorage())
                    && existing.getColor().equals(phone.getColor())
                    && Double.compare(existing.getPrice(), phone.getPrice()) == 0;
        
        // If error was thrown when creating phone return false.
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // replaceInventoryFile method writes inventory lines to a temporary file and replaces the inventory
    // file atomically (all-or-nothing) when supported.
    private static void replaceInventoryFile(Path path, List<String> lines) throws IOException {
        
        // Define absolute file path and temporary file path.
        Path absolutePath = path.toAbsolutePath();
        Path temporaryFile = Files.createTempFile(absolutePath.getParent(), ".inventory-", ".tmp");
        
        // Attempt to write lines to the temporary file and replace the original file accordingly.
        try {
        
            // Write lines to temporary file
            Files.write(temporaryFile, lines, StandardCharsets.UTF_8);

            // Attempt to move the temporary file to the path atomically, replacing the existing.
            try {
                Files.move(
                        temporaryFile,
                        absolutePath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);

            // If the atomic replacement fails, replace normally.
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }

        // Delete temporary file.
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    // InvalidFiltersException indicates that a search request contains a null or invalid filter.
    public static class InvalidFiltersException extends IllegalArgumentException {
        public InvalidFiltersException(String message) {
            super(message);
        }
    }
}