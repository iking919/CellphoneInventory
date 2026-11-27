package CellphoneInventory;

/*
    The PhoneWriteRequest record represents phone values received in HTTP add,
    update, and delete requests. It converts these values into a Phone object
    and validates text fields before they are written to the inventory file.
*/
public record PhoneWriteRequest(
        String brand,
        String model,
        String storage,
        String color,
        Double price) {

    // toPhone method converts the request values into a Phone object.
    // Text fields are normalized and checked for invalid CSV characters.
    public Phone toPhone() {
        return new Phone(
                csvField(brand, "brand"),
                csvField(model, "model"),
                csvField(storage, "storage"),
                csvField(color, "color"),
                price);
    }

    // csvField method takes in a string value and field name, throwing an
    // IllegalArgumentException if the value is missing or contains CSV characters.
    private static String csvField(String value, String field) {

        // If value is empty return exception.
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }

        // Remove surrounding whitespace from the field.
        String normalized = value.strip();

        // If the string contains ","  or line breaks return exception since they are invalid formats.
        if (normalized.indexOf(',') >= 0 || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(field + " cannot contain commas or line breaks.");
        }

        // Return normalized string.
        return normalized;
    }
}
