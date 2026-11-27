/*
    delete-phone.js contains logic for deleting phones from the inventory.
    It identifies the selected phone, displays a confirmation dialog, and
    sends a DELETE request to the Spring Boot API.
*/

// Store references to the delete dialog and its status and action elements.
const deleteDialog = document.querySelector("#delete-confirmation");
const deleteForm = document.querySelector("#delete-form");
const cancelDeleteButton = document.querySelector("#cancel-delete");
const deleteStatusMessage = document.querySelector("#inventory-status");
const deleteConfirmationMessage = document.querySelector("#delete-confirmation-message");
const confirmDeleteButton = document.querySelector("#confirm-delete");

let selectedPhone = null;
let selectedRow = null;

// document click handler identifies the delete button and opens the confirmation dialog.
document.addEventListener("click", (event) => {

    // Ensure the clicked target is a valid HTML element.
    if (!(event.target instanceof Element)) {
        return;
    }

    // Ignore clicks that were not made on a delete button.
    const deleteButton = event.target.closest(".delete-button");
    if (!deleteButton) {
        return;
    }

    // Build the selected phone object from the delete button's data attributes.
    const { brand, model, storage, color, price, displayPrice } = deleteButton.dataset;
    selectedPhone = {
        brand,
        model,
        storage,
        color,
        price: Number(price)
    };

    // Store the selected table row and describe the phone in the confirmation message.
    selectedRow = deleteButton.closest("tr");
    deleteConfirmationMessage.textContent =
        `Are you sure you want to delete the ${brand} ${model} (${storage}, ${color}, ${displayPrice})?`;

    // Open the delete confirmation dialog.
    deleteDialog.showModal();
});

// deleteForm submit handler sends the selected phone to the inventory API.
deleteForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    // Stop the delete operation if no phone is selected.
    if (!selectedPhone) {
        return;
    }

    // Disable the confirmation button while the request is in progress.
    confirmDeleteButton.disabled = true;
    try {
        // Send the selected phone to the delete endpoint.
        const response = await fetch("/api/phones", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selectedPhone)
        });

        if (!response.ok) {
            throw new Error(`Delete request failed with status ${response.status}: ${await response.text()}`);
        }

        // Remove the deleted row and update the remaining inventory count.
        selectedRow.remove();
        const remainingCount = document.querySelectorAll("#inventory-table tbody tr").length;
        deleteStatusMessage.textContent = `Phone deleted. ${remainingCount} phones remain.`;

        // Close the dialog and clear the selected phone and row.
        deleteDialog.close();
        selectedPhone = null;
        selectedRow = null;
    } catch (error) {
        // Display an error when the delete request fails.
        deleteConfirmationMessage.textContent = "Could not delete the phone. Check the browser console for details.";
        console.error("Phone delete failed:", error);
    } finally {
        confirmDeleteButton.disabled = false;
    }
});

// cancelDeleteButton click handler closes the confirmation dialog.
cancelDeleteButton.addEventListener("click", () => {
    deleteDialog.close();
});
