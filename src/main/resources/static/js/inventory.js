/*
    inventory.js contains shared logic for loading and displaying phone inventory.
    It validates phone objects, formats table values, and provides column sorting.
*/

// Store references to the inventory table and its status element.
const inventoryTable = document.querySelector("#inventory-table");
const inventoryBody = inventoryTable.querySelector("tbody");
const inventoryStatus = document.querySelector("#inventory-status");

// Create formatters for phone prices and text sorting.
const priceFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
});
const textCollator = new Intl.Collator(undefined, { numeric: true, sensitivity: "base" });
const sortColumnIndexes = {
    brand: 0,
    model: 1,
    storage: 2,
    color: 3,
    price: 4
};
let sortState = { field: null, direction: 1 };

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

// storageSortValue method converts storage text into a number for numeric sorting.
function storageSortValue(storage) {
    const match = storage.match(/^(\d+)\s*(GB|TB)$/i);
    if (!match) {
        return storage;
    }

    return Number(match[1]) * (match[2].toUpperCase() === "TB" ? 1000 : 1);
}

// sortInventoryRows method sorts the displayed inventory rows using the selected 
// column and sort direction. It exits without changes if no column has been selected.
function sortInventoryRows() {
    if (!sortState.field) {
        return;
    }

    // Sort table rows using the active column and direction.
    const columnIndex = sortColumnIndexes[sortState.field];
    const rows = Array.from(inventoryBody.rows);
    rows.sort((firstRow, secondRow) => {
        const firstValue = firstRow.cells[columnIndex].dataset.sortValue;
        const secondValue = secondRow.cells[columnIndex].dataset.sortValue;
        const comparison = sortState.field === "price" || sortState.field === "storage"
            ? Number(firstValue) - Number(secondValue)
            : textCollator.compare(firstValue, secondValue);
        return comparison * sortState.direction;
    });
    inventoryBody.replaceChildren(...rows);
}

// updateSortIndicators method updates sorting icons and accessibility attributes.
function updateSortIndicators() {
    inventoryTable.querySelectorAll(".sort-button").forEach((button) => {
        const active = button.dataset.sort === sortState.field;
        const indicator = button.querySelector(".sort-indicator");
        const heading = button.closest("th");

        indicator.textContent = active
            ? (sortState.direction === 1 ? "\u2191" : "\u2193")
            : "\u2195";
        heading.setAttribute(
            "aria-sort",
            active ? (sortState.direction === 1 ? "ascending" : "descending") : "none");
    });
}

// displayInventory method creates table rows from a list of phone objects.
function displayInventory(phones) {

    // Create a table row containing all five phone properties.
    const rows = phones.map((phone, index) => {
        const row = document.createElement("tr");
        const values = [
            phone.brand,
            phone.model,
            phone.storage,
            phone.color,
            priceFormatter.format(phone.price)
        ];
        const sortValues = [
            phone.brand,
            phone.model,
            storageSortValue(phone.storage),
            phone.color,
            phone.price
        ];

        // Create a table data cell for each displayed phone value.
        values.forEach((value, valueIndex) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            cell.dataset.sortValue = String(sortValues[valueIndex]);
            row.appendChild(cell);
        });

        // Add edit and delete buttons when the table is in manage mode.
        if (inventoryTable.dataset.allowEdit === "true") {

            // Create an edit button containing the selected phone's values.
            const editCell = document.createElement("td");
            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "edit-button";
            editButton.textContent = "Edit";
            editButton.dataset.phoneIndex = String(index);

            // Include phone attributes for editting fields popup.
            editButton.dataset.brand = phone.brand;
            editButton.dataset.model = phone.model;
            editButton.dataset.storage = phone.storage;
            editButton.dataset.color = phone.color;
            editButton.dataset.price = String(phone.price);
            editCell.appendChild(editButton);
            row.appendChild(editCell);

            // Create a delete button containing the selected phone's values.
            const deleteCell = document.createElement("td");
            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "delete-button";
            deleteButton.textContent = "Delete";

            // Store displayed deletion values for the confirmation popup.
            deleteButton.dataset.brand = phone.brand;
            deleteButton.dataset.model = phone.model;
            deleteButton.dataset.storage = phone.storage;
            deleteButton.dataset.color = phone.color;
            deleteButton.dataset.price = String(phone.price);
            deleteButton.dataset.displayPrice = priceFormatter.format(phone.price);
            deleteCell.appendChild(deleteButton);
            row.appendChild(deleteCell);
        }

        return row;
    });

    // Replace the previous table rows and reapply the active sort.
    inventoryBody.replaceChildren(...rows);
    sortInventoryRows();
    updateSortIndicators();
}

window.displayInventory = displayInventory;

// inventoryTable click handler changes or reverses the active sort column.
inventoryTable.addEventListener("click", (event) => {
    if (!(event.target instanceof Element)) {
        return;
    }

    const sortButton = event.target.closest(".sort-button");
    if (!sortButton || !inventoryTable.contains(sortButton)) {
        return;
    }

    const field = sortButton.dataset.sort;
    if (!Object.prototype.hasOwnProperty.call(sortColumnIndexes, field)) {
        return;
    }

    if (sortState.field === field) {
        sortState.direction *= -1;
    } else {
        sortState = { field, direction: 1 };
    }

    sortInventoryRows();
    updateSortIndicators();
});

// loadInventory method fetches phone inventory from the API and displays it in the table.
async function loadInventory() {
    try {

        // Throw an error when the inventory request fails.
        const response = await fetch("/api/phones");
        if (!response.ok) {
            throw new Error(`Inventory request failed with status ${response.status}.`);
        }

        // Validate the inventory response before displaying it.
        const phones = await response.json();
        if (!Array.isArray(phones) || !phones.every(isPhone)) {
            throw new Error("Inventory data must be an array of valid phone records.");
        }

        // Display the phones and update the inventory status.
        displayInventory(phones);
        inventoryStatus.textContent = `${phones.length} phones loaded.`;
        return true;

    } catch (error) {

        // Display an error and stop loading when the inventory request fails.
        inventoryStatus.textContent = "Could not load inventory from the server.";
        console.error("Inventory load failed:", error);
        return false;
    }
}

window.loadInventory = loadInventory;

// Load inventory automatically when the table requests it.
if (inventoryTable && inventoryTable.dataset.loadInventory === "true") {
    loadInventory();
}
