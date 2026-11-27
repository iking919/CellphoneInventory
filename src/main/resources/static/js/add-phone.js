/*
    add-phone.js contains logic for adding phones to the existing inventory. It validates 
    form fields, displays a confirmation dialog, and sends a POST request to the Spring Boot API.
*/

// Store references to the add phone form and its status and confirmation elements.
const addPhoneForm = document.querySelector("#add-phone-form");
const addPhoneStatus = document.querySelector("#add-phone-status");
const addPhoneDialog = document.querySelector("#add-confirmation");
const requiredFields = Array.from(addPhoneForm.querySelectorAll("[required]"));

// validateAddPhoneForm method checks each required field and updates its validation styling.
function validateAddPhoneForm() {
    let formIsValid = true;

    // Check that every required field is non-empty and valid.
    requiredFields.forEach((field) => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);
        if (!fieldIsValid) {
            formIsValid = false;
        }
    });

    // Display a warning when the form contains invalid or empty fields.
    addPhoneStatus.textContent = formIsValid
        ? ""
        : "Please fill in all fields with valid values.";
    addPhoneStatus.classList.toggle("error", !formIsValid);

    return formIsValid;
}

// addPhoneForm submit handler prevents the default submission and displays the confirmation dialog.
addPhoneForm.addEventListener("submit", (event) => {
    event.preventDefault();

    // Open the confirmation dialog only when all form fields are valid.
    if (validateAddPhoneForm()) {
        addPhoneDialog.showModal();
    }
});

// Required field input handlers update validation styling while the user edits the form.
requiredFields.forEach((field) => {
    field.addEventListener("input", () => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);

        // Clear the status message when all fields become valid.
        if (!requiredFields.some((requiredField) => requiredField.classList.contains("invalid"))) {
            addPhoneStatus.textContent = "";
            addPhoneStatus.classList.remove("error");
        }
    });
});

// addPhoneDialog close handler sends a phone to the inventory API after confirmation.
addPhoneDialog.addEventListener("close", async () => {

    // Cancel the operation when the user closes the dialog without confirming.
    if (addPhoneDialog.returnValue !== "confirm") {
        return;
    }

    // Build a phone object using the values entered in the form.
    const phone = {
        brand: addPhoneForm.elements.brand.value.trim(),
        model: addPhoneForm.elements.model.value.trim(),
        storage: addPhoneForm.elements.storage.value.trim(),
        color: addPhoneForm.elements.color.value.trim(),
        price: Number(addPhoneForm.elements.price.value)
    };

    // Disable the submit button while the request is in progress.
    const addButton = document.querySelector("#add-phone-button");
    addButton.disabled = true;

    try {
        
        // Send the new phone to the backend.
        const response = await fetch("/api/phones", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(phone)
        });

        if (!response.ok) {
            throw new Error(`Add request failed with status ${response.status}: ${await response.text()}`);
        }

        // Reset the form and display a successful submission status.
        addPhoneForm.reset();
        requiredFields.forEach((field) => field.classList.remove("invalid"));
        addPhoneStatus.textContent = "Phone added to inventory.";
        addPhoneStatus.classList.remove("error");
    } catch (error) {

        // Display an error when the add request fails.
        addPhoneStatus.textContent = "Could not add phone. Check the browser console for details.";
        addPhoneStatus.classList.add("error");
        console.error("Phone add failed:", error);
    } finally {
        addButton.disabled = false;
    }
});
