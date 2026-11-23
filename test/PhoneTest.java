import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// PhoneTest verifies that valid inventory fields are retained and invalid fields are rejected during phone object construction.
class PhoneTest {

    // createsPhoneWithValidValues tests that a valid phone record can be created and read back.
    @Test
    void createsPhoneWithValidValues() {

        // Initialize one valid phone.
        Phone phone = new Phone("Google", "Pixel 8", "128 GB", "Black", 799.99);

        // Ensure the constructor preserved the brand, model, storage, color, and price.
        assertEquals("Google", phone.getBrand());
        assertEquals("Pixel 8", phone.getModel());
        assertEquals("128 GB", phone.getStorage());
        assertEquals("Black", phone.getColor());
        assertEquals(799.99, phone.getPrice());
    }

    // rejectsMissingTextFields tests that required text values cannot be null or blank.
    @Test
    void rejectsMissingTextFields() {

        // Initialize separate invalid records for a null brand, blank model, and null color.
        // Each construction should fail independently with an input validation exception.
        assertThrows(IllegalArgumentException.class,
                () -> new Phone(null, "Pixel 8", "128 GB", "Black", 799.99));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", " ", "128 GB", "Black", 799.99));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", null, 799.99));
    }

    // rejectsInvalidStorage tests that storage must be present, positive, and use an allowed unit.
    @Test
    void rejectsInvalidStorage() {

        // Initialize records with missing storage, zero capacity, and an unsupported MB unit.
        // These invalid values should not produce Phone objects.
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", null, "Black", 799.99));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "0 GB", "Black", 799.99));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 MB", "Black", 799.99));
    }

    // rejectsInvalidPrices tests that price must be present, positive, and finite.
    @Test
    void rejectsInvalidPrices() {

        // Initialize records with null, zero, negative, NaN, and infinite prices.
        // These invalid values should not produce Phone objects.
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", "Black", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", "Black", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", "Black", -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", "Black", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new Phone("Google", "Pixel 8", "128 GB", "Black", Double.POSITIVE_INFINITY));
    }
}
