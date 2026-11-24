package CellphoneInventory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// InventoryServiceTest checks that filter validation, matching, and the ordering rules applied to search results.
class InventoryServiceTest {

    // filtersGooglePhonesBelowPriceLimitAndSortsAscending tests the provided Google and price filter example in the handout.
    @Test
    void filtersGooglePhonesBelowPriceLimitAndSortsAscending() {

        // Initialize an unsorted dataset with unrelated phones and a Google phone exactly = $900.
        // The  "< $900" criteria should exclude that boundary phone and all non-Google phones.
        List<Phone> phones = List.of(
                phone("Google", "Pixel 8 Pro", "128 GB", "Black", 900.00),
                phone("Google", "Pixel 8", "256 GB", "Gray", 859.99),
                phone("Samsung", "Galaxy S24", "128 GB", "Black", 699.99),
                phone("Google", "Pixel 6a", "256 GB", "Gray", 399.99),
                phone("Google", "Pixel 8", "128 GB", "Black", 799.99),
                phone("Google", "Pixel 7a", "256 GB", "Gray", 549.99));

        // User filterPhones to apply the filter to the phone list, in the same manner the application will do for its users.
        List<Phone> result = InventoryService.filterPhones(
                phones, List.of("Brand => Google", "Price => < $900"));

        // Ensure the results are ordered by increasing price and contain only Google phones < $900.
        assertEquals(List.of("Pixel 6a", "Pixel 7a", "Pixel 8", "Pixel 8"),
                result.stream().map(Phone::getModel).toList());
        assertEquals(List.of(399.99, 549.99, 799.99, 859.99),
                result.stream().map(Phone::getPrice).toList());
        assertTrue(result.stream().allMatch(phone -> phone.getBrand().equals("Google")));
        assertTrue(result.stream().allMatch(phone -> phone.getPrice() < 900));
    }

    // filtersByStorageAndMultipleColorsThenSortsByBrandAndColor tests the provided storage and color example.
    @Test
    void filtersByStorageAndMultipleColorsThenSortsByBrandAndColor() {

        // Initialize phones with the valid and invalid storage/color combinations according to the criteria.
        // Blue and non-128 GB phones must be excluded.
        List<Phone> phones = List.of(
                phone("Samsung", "Galaxy", "128 GB", "Opalite Gray", 700),
                phone("Google", "Pixel", "256 GB", "Black", 500),
                phone("Apple", "iPhone", "128 GB", "Gray", 800),
                phone("Samsung", "Galaxy", "128 GB", "Black", 700),
                phone("Apple", "iPhone", "128 GB", "Black", 800),
                phone("Google", "Pixel", "128 GB", "Gray", 500),
                phone("Google", "Pixel", "128 GB", "Obsidian Black", 500),
                phone("Samsung", "Galaxy", "128 GB", "Blue", 700));

        // Filter for exactly 128 GB and either requested color, matching the example parameter list.
        List<Phone> result = InventoryService.filterPhones(
                phones, List.of("Storage => 128 GB", "Color => Black, Gray"));

        // Ensure every matching brand/color pair is present and ordered by brand first, then color name.
        assertEquals(List.of(
                "Apple:Black",
                "Apple:Gray",
                "Google:Gray",
                "Google:Obsidian Black",
                "Samsung:Black",
                "Samsung:Opalite Gray"),
                result.stream().map(phone -> phone.getBrand() + ":" + phone.getColor()).toList());
        assertTrue(result.stream().allMatch(phone -> phone.getStorage().equals("128 GB")));
    }

    // matchesStorageComparisonsAcrossGbAndTbUnits tests that storage comparisons convert units consistently.
    @Test
    void matchesStorageComparisonsAcrossGbAndTbUnits() {

        // Initialize phones below, at, and above the 512 GB threshold, including a 1 TB capacity.
        List<Phone> phones = List.of(
                phone("Google", "Pixel A", "512 GB", "Black", 500),
                phone("Google", "Pixel B", "1 TB", "Black", 600),
                phone("Google", "Pixel C", "128 GB", "Black", 400));

        // Apply a ""> 512GB" filter. The TB value must compare as larger than 512 GB.
        List<Phone> result = InventoryService.filterPhones(
                phones, List.of("Storage => > 512 GB"));

        // Ensure only the 1 TB phone matches; the phone exactly at 512 GB is excluded.
        assertEquals(List.of("Pixel B"), result.stream().map(Phone::getModel).toList());
    }

    // returnsSortedCopyWhenNoFiltersAreApplied tests sorting without changing the original list of phones.
    @Test
    void returnsSortedCopyWhenNoFiltersAreApplied() {

        // Initialize an inventory list in reverse brand order.
        List<Phone> phones = new ArrayList<>(List.of(
                phone("Google", "Pixel", "128 GB", "Gray", 500),
                phone("Apple", "iPhone", "128 GB", "Black", 700)));

        // Save the first phone in the unsorted list for future comparison
        Phone originalFirst = phones.get(0);

        // Filter with no criteria. The service still returns its default sorted inventory.
        List<Phone> result = InventoryService.filterPhones(phones, List.of());

        // Ensure the result is a separate, sorted list.
        assertNotSame(phones, result);

        // Ensure the original list of phones remain inteact.
        assertSame(originalFirst, phones.get(0));
        assertEquals(List.of("Apple", "Google"), result.stream().map(Phone::getBrand).toList());
    }

    // acceptsValidFilterFormsWithoutCaseSensitivity tests accepted syntax for every supported phone field.
    @Test
    void acceptsValidFilterFormsWithoutCaseSensitivity() {
        // Check the five supported fields using different capitalization and representative price/storage values.
        assertTrue(InventoryService.validFilter("brand => Google"));
        assertTrue(InventoryService.validFilter("Color => Black, Gray"));
        assertTrue(InventoryService.validFilter("Price => < $900"));
        assertTrue(InventoryService.validFilter("Storage => 128GB"));
        assertTrue(InventoryService.validFilter("Model => Pixel"));
        assertTrue(InventoryService.validFilter("BRAND => google"));
        assertTrue(InventoryService.validFilter("color => black, GRAY"));
        assertTrue(InventoryService.validFilter("Price => $900"));
        assertTrue(InventoryService.validFilter("Storage => > 128 GB"));
        assertTrue(InventoryService.validFilter("Model => PIXEL"));
    }

    // rejectsInvalidFilters ensures filters with invalid syntax and invalid typed parameters are rejected.
    @Test
    void rejectsInvalidFilters() {

        // Initialize invalid filters with missing separators or values, unknown fields, and empty alternatives.
        // Include invalid price operators/numbers and zero, negative, unsupported, or overflowing storage values.
        List<String> invalidFilters = List.of(
                "",
                "Brand Google",
                "Unknown => value",
                "Brand => ",
                "Brand => Google => Pixel",
                "Brand => Google,",
                "Price => < $oops",
                "Price => >= $900",
                "Price => NaN",
                "Storage => 0 GB",
                "Storage => -128 GB",
                "Storage => 128 MB",
                "Storage => 999999999999999 GB");

        // Ensure validation rejects every invalid value before matching can parse or apply it.
        for (String filter : invalidFilters) {
            assertFalse(InventoryService.validFilter(filter), filter);
        }
    }

    // rejectsNullFilter tests handling of a missing filter value.
    @Test
    void rejectsNullFilter() {
        // Pass a null value directly to the validator and ensure it returns false instead of throwing.
        assertFalse(InventoryService.validFilter(null));
    }

    // phone creates a validated Phone object so each test exercises the same model used by the application.
    private static Phone phone(String brand, String model, String storage, String color, double price) {
        return new Phone(brand, model, storage, color, price);
    }
}
