// Change this to the Spring Boot endpoint when it is ready.
const inventoryUrl = "mock/phones.json";

// Establish entry for phone table and inventory status text.
const inventoryTable = document.querySelector("#inventory-table");
const inventoryBody = inventoryTable.querySelector("tbody");
const inventoryStatus = document.querySelector("#inventory-status");

// Use US currency in table.
const priceFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
});


// isPhone function returns true if input phone is a valid phone with necessary fields.
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

// Display inventory takes json list of phones and displays them in table.
function displayInventory(phones) {
    
    // For each phone create a table row with all 5 properties.
    const rows = phones.map((phone) => {
        const row = document.createElement("tr");
        const values = [
            phone.brand,
            phone.model,
            phone.storage,
            phone.color,
            priceFormatter.format(phone.price)
        ];

        // For each phone value create a corresponding table data cell.
        values.forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        // Display delete button on remove inventory page
        if (inventoryTable.dataset.allowDelete === "true") {
            const actionCell = document.createElement("td");
            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "delete-button";
            deleteButton.textContent = "Delete";
            actionCell.appendChild(deleteButton);
            row.appendChild(actionCell);
        }

        return row;
    });

    // Replace the previous inventory body section with the updated table.
    inventoryBody.replaceChildren(...rows);
}

// Fetch the inventory json and load into list of phones
async function loadInventory() {
    
    // Try block attempts to fetch the inventory json, ensure all entries are phones,
    // and display inventory table accordingly.
    try {
        
        // Throw error if fetch request fails.
        const response = await fetch(inventoryUrl);
        if (!response.ok) {
            throw new Error(`Inventory request failed with status ${response.status}.`);
        }

        // Throw error if inventory data is not an array of valid phones.
        const phones = await response.json();
        if (!Array.isArray(phones) || !phones.every(isPhone)) {
            throw new Error("Inventory data must be an array of valid phone records.");
        }

        // Display phone inventory, and provide inventory loaded text.
        displayInventory(phones);
        inventoryStatus.textContent = `${phones.length} phones loaded.`;

    // Write any errors to content and return warning text.
    } catch (error) {
        inventoryStatus.textContent = "Could not load inventory. Check mock/phones.json and use a local web server.";
        console.error("Inventory load failed:", error);
    }
}

// Call load inventory function to run through all logic.
loadInventory();
