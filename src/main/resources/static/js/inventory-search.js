/*
    inventory-search.js contains logic for filtering phones in the inventory.
    It loads brand and storage options, collects selected filters, and sends
    a search request to the Spring Boot API.
*/

// Store references to the search form, results, status, and filter controls.
const searchForm = document.querySelector("#search-form");
const resultsSection = document.querySelector("#search-results");
const searchStatus = document.querySelector("#search-status");
const resultsStatus = document.querySelector("#inventory-status");
const clearFiltersButton = document.querySelector("#clear-filters-button");
const searchButton = document.querySelector("#search-button");
const brandSelect = document.querySelector("#brand");
const storageSelect = document.querySelector("#storage");

// isPhone method checks whether an inventory object has valid phone fields.
function isPhone(phone) {
    return phone !== null
        && typeof phone === "object"
        && typeof phone.brand === "string"
        && typeof phone.model === "string"
        && typeof phone.storage === "string"
        && typeof phone.color === "string"
        && typeof phone.price === "number"
        && Number.isFinite(phone.price);
}

// storageCapacity method converts storage text into a number for sorting select options.
function storageCapacity(storage) {
    const match = storage.match(/^(\d+)\s*(GB|TB)$/i);
    if (!match) {
        return Number.MAX_SAFE_INTEGER;
    }

    // Handle instances of "TB"
    return Number(match[1]) * (match[2].toUpperCase() === "TB" ? 1000 : 1);
}

// setSelectOptions method replaces a dropdown's options while preserving its default option.
function setSelectOptions(select, values) {
    const defaultOption = select.options[0];
    const options = values.map((value) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        return option;
    });
    select.replaceChildren(defaultOption, ...options);
}

// loadFilterOptions method loads brand and storage options from the inventory API.
async function loadFilterOptions() {
    const textCollator = new Intl.Collator(undefined, { numeric: true, sensitivity: "base" });
    try {

        // Load the current inventory for dynamic brand and storage options.
        const response = await fetch("/api/phones");
        if (!response.ok) {
            throw new Error(`Inventory request failed with status ${response.status}.`);
        }

        // Validate the inventory response before populating the dropdowns.
        const phones = await response.json();
        if (!Array.isArray(phones) || !phones.every(isPhone)) {
            throw new Error("Inventory data must be an array of valid phone records.");
        }

        // Create sorted lists of unique brand and storage values.
        const brands = [...new Set(phones.map((phone) => phone.brand))].sort(textCollator.compare);
        const storageOptions = [...new Set(phones.map((phone) => phone.storage))]
            .sort((first, second) => storageCapacity(first) - storageCapacity(second));
        setSelectOptions(brandSelect, brands);
        setSelectOptions(storageSelect, storageOptions);

    } catch (error) {

        // Display an error when filter options cannot be loaded.
        searchStatus.textContent = "Could not load brand and storage options from inventory.";
        console.error("Inventory filter options failed to load:", error);
    } finally {

        // Enable the dropdowns after the options finish loading.
        brandSelect.disabled = false;
        storageSelect.disabled = false;
    }
}

// formatCurrencyInputs method removes invalid characters and allows only one decimal point.
document.querySelectorAll(".currency-input input").forEach((input) => {
    const updatePrice = () => {
        input.value = input.value.replace(/[^0-9.]/g, "").replace(/(\..*)\./g, "$1");
    };

    // Run updatePrice whenever the input value changes.
    input.addEventListener("input", updatePrice);
    updatePrice();
});

// clearFiltersButton click handler resets the form and returns to the search view.
clearFiltersButton.addEventListener("click", () => {

    // Reset search values and return to the search form.
    searchForm.reset();
    resultsSection.hidden = true;
    searchForm.hidden = false;
    searchStatus.textContent = "";
    resultsStatus.textContent = "";
});

// searchForm submit handler builds filters and sends them to the inventory search API.
searchForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    // Disable the submit button and show that a search is in progress.
    searchStatus.textContent = "Searching inventory...";
    searchButton.disabled = true;

    // Collect text filters from the brand, model, storage, and color fields.
    const filters = [];
    const fields = ["brand", "model", "storage", "color"];

    fields.forEach((field) => {
        const value = document.querySelector(`#${field}`).value.trim();
        if (value) {
            filters.push(`${field} => ${value}`);
        }
    });

    // Add minimum and maximum price filters when values are entered.
    const minPrice = document.querySelector("#minPrice").value.trim();
    const maxPrice = document.querySelector("#maxPrice").value.trim();
    if (minPrice) {
        filters.push(`price => > $${minPrice}`);
    }
    if (maxPrice) {
        filters.push(`price => < $${maxPrice}`);
    }

    try {

        // Send the filters to the inventory search API.
        const response = await fetch("/api/phones/search", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(filters)
        });

        if (!response.ok) {
            throw new Error(`Search request failed with status ${response.status}: ${await response.text()}`);
        }

        // Validate and display the filtered inventory results.
        const phones = await response.json();
        if (!Array.isArray(phones)) {
            throw new Error("Search response must be an array of phone records.");
        }

        console.log("Search results:", phones);
        window.displayInventory(phones);
        resultsStatus.textContent = phones.length === 0
            ? "No phones match these filters."
            : `${phones.length} phones found.`;
        searchStatus.textContent = "";
        searchForm.hidden = true;
        resultsSection.hidden = false;
    } catch (error) {

        // Display an error when the search request fails.
        searchStatus.textContent = "Search failed. Check the browser console for details.";
        console.error("Inventory search failed:", error);
    } finally {
        
        // Re-enable the submit button after the search finishes.
        searchButton.disabled = false;
    }
});

// Load dynamic filter options when the search page initializes.
loadFilterOptions();
