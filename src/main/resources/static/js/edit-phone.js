/*
    edit-phone.js contains logic for updating phones in the existing inventory.
    It identifies the selected phone, validates the edit form, and sends a
    PUT request to the Spring Boot API.
*/

(() => {

// Store references to the edit dialog and its form and status elements.
const editDialog = document.querySelector("#edit-confirmation");
const editForm = document.querySelector("#edit-phone-form");
const editStatus = document.querySelector("#edit-phone-status");
const cancelEditButton = document.querySelector("#cancel-edit");
const saveEditButton = document.querySelector("#save-edit");
const inventoryStatusMessage = document.querySelector("#inventory-status");

let selectedPhone = null;

// document click handler identifies the edit button and opens the confirmation dialog.
document.addEventListener("click", (event) => {

    // Ensure the clicked target is a valid HTML element.
    if (!(event.target instanceof Element)) {
        return;
    }

    // Ignore clicks that were not made on an edit button.
    const editButton = event.target.closest(".edit-button");
    if (!editButton) {
        return;
    }

    // Build the selected phone object from the edit button's data attributes.
    selectedPhone = {
        brand: editButton.dataset.brand,
        model: editButton.dataset.model,
        storage: editButton.dataset.storage,
        color: editButton.dataset.color,
        price: Number(editButton.dataset.price)
    };

    // Populate the edit form with the selected phone's current values.
    editForm.elements.brand.value = selectedPhone.brand;
    editForm.elements.model.value = selectedPhone.model;
    editForm.elements.storage.value = selectedPhone.storage;
    editForm.elements.color.value = selectedPhone.color;
    editForm.elements.price.value = selectedPhone.price;
    editStatus.textContent = "";
    editStatus.classList.remove("error");

    // Open the edit confirmation dialog.
    editDialog.showModal();
});

// editForm submit handler validates and sends the updated phone to the inventory API.
editForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    // Check that every required field is non-empty and valid.
    const requiredFields = Array.from(editForm.querySelectorAll("[required]"));
    let formIsValid = true;
    requiredFields.forEach((field) => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);
        if (!fieldIsValid) {
            formIsValid = false;
        }
    });

    // Display a warning and stop the update when the form is invalid.
    if (!formIsValid) {
        editStatus.textContent = "Please fill in all fields with valid values.";
        editStatus.classList.add("error");
        return;
    }

    // Build an updated phone object using the values entered in the form.
    const updatedPhone = {
        brand: editForm.elements.brand.value.trim(),
        model: editForm.elements.model.value.trim(),
        storage: editForm.elements.storage.value.trim(),
        color: editForm.elements.color.value.trim(),
        price: Number(editForm.elements.price.value)
    };

    // Disable the save button and display a saving status while the request runs.
    saveEditButton.disabled = true;
    editStatus.textContent = "Saving changes...";
    editStatus.classList.remove("error");

    try {
        // Send the original and updated phones to the update endpoint.
        const response = await fetch("/api/phones", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                original: selectedPhone,
                updated: updatedPhone
            })
        });

        if (!response.ok) {
            throw new Error(`Update request failed with status ${response.status}: ${await response.text()}`);
        }

        // Close the dialog and refresh the inventory display.
        editDialog.close();
        selectedPhone = null;
        const inventoryReloaded = await window.loadInventory();
        inventoryStatusMessage.textContent = inventoryReloaded
            ? "Phone updated."
            : "Phone updated, but the inventory display could not be refreshed.";
    } catch (error) {
        
        // Display an error when the update request fails.
        editStatus.textContent = "Could not update phone. Check the browser console for details.";
        editStatus.classList.add("error");
        console.error("Phone update failed:", error);
    } finally {
        saveEditButton.disabled = false;
    }
});

// cancelEditButton click handler closes the confirmation dialog.
cancelEditButton.addEventListener("click", () => {
    editDialog.close();
});
})();