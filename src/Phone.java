/*
    The Phone class represents individual phones in the store's inventory.
    All filtering and inventory operations are built around these objects.
*/

public class Phone {

    // Each phone object has a brand, model, storage, color and price attribute. This can be extended accordingly. 
    // Price attribute is a double, all other attributes are string.
    private String brand;
    private String model;
    private String storage;
    private String color;
    private Double price;

    // Phone initialization class takes in brand, model, storage, color and price, and creates a phone object accordingly.
    public Phone(String brand, String model, String storage, String color, Double price) {
        
        // Use requireText to ensure brand field isn't null or empty, throwing illegal argument exception otherwise.
        this.brand = requireText(brand, "brand");

        // Use requireText to ensure model field isn't null or empty, throwing illegal argument exception otherwise.
        this.model = requireText(model, "model");

        // Use validateStorage to normalize storage field, while ensure storage field is valid, throwing illegal argument exception otherwise.
        this.storage = validateStorage(storage);

        // Use requireText to ensure color field isn't null or empty, throwing illegal argument exception otherwise.
        this.color = requireText(color, "color");

        // If price field is null or if it's not a positive double, throw illegal argument exception.
        if (price == null || !Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Price must be a finite number greater than zero.");
        }
        this.price = price;
    }

    // requireText method takes in a string value and string field, throwing an illegal argument exception if the value is null or blank.
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
        return value;
    }

    // validateStorage method takes in a string storage value and throws an illegal argument exception if it is not valid.
    private static String validateStorage(String storage) {

        // If storage value is null or blank throw illegal argument exception.
        if (storage == null || storage.isBlank()) {
            throw new IllegalArgumentException("Storage must not be empty.");
        }

        // Remove whitespace from input.
        String normalizedStorage = storage.strip();

        // Use matching call to ensure storage value is a whole number followed by "GB" or "TB" regardless of case.
        if (!normalizedStorage.matches("(?i)\\d+\\s*(GB|TB)")) {
            throw new IllegalArgumentException("Storage must be a positive capacity in GB or TB.");
        }

        // Use replaceAll to remove "GB" or "TB" from the end of the storage string, leaving just the numeric storage capacity.
        String capacityText = normalizedStorage.replaceAll("(?i)\\s*(GB|TB)$", "");

        // Attempt to convert the storage capacity to a long. If the conversion fails, throw an illegal argument exception.
        try {

            // If capacity isn't positive throw an illegal argument exception.
            if (Long.parseLong(capacityText) <= 0) {
                throw new IllegalArgumentException("Storage capacity must be greater than zero.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Storage capacity is out of range.", e);
        }

        // If all checks pass, return the normalized storage string.
        return normalizedStorage;
    }

    
    // Public getBrand(), getModel(), getStorage(), getColor() and getPrice() methods are used to retrieve the phone object's corresponding attributes.
    // These are used during filtering and sorting.

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getStorage() {
        return storage;
    }

    public String getColor() {
        return color;
    }

    public Double getPrice() {
        return price;
    }
}