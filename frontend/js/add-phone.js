/* 
    add-phone.js contains logic to add phones to existing inventory. This includes
    a form with all needed inputs and field checking, along with inventory creation itself.
    
    Spring boot is needed to properly connect backend logic.
*/

// Establish entry points for add phone form and confirmation button.
const addPhoneForm = document.querySelector("#add-phone-form");
const addPhoneStatus = document.querySelector("#add-phone-status");
const addPhoneDialog = document.querySelector("#add-confirmation");
const requiredFields = Array.from(addPhoneForm.querySelectorAll("[required]"));

// Validate that all fields are filled before creating allowing a phone addition
function validateAddPhoneForm() {
    let formIsValid = true;

    // Ensure all fields are valid and non-empty.
    requiredFields.forEach((field) => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);
        if (!fieldIsValid) {
            formIsValid = false;
        }
    });

    // If any form is not valid or empty highlight and output warning.
    addPhoneStatus.textContent = formIsValid
        ? ""
        : "Please fill in all fields with valid values.";
    addPhoneStatus.classList.toggle("error", !formIsValid);

    return formIsValid;
}

// Handle submit button clicks.
addPhoneForm.addEventListener("submit", (event) => {
    // Prevent browser default form submission and page reload.
    event.preventDefault();
    
    // Check valid data and display popup.
    if (validateAddPhoneForm()) {
        addPhoneDialog.showModal();
    }
});

// Look through each field, updating red styling if field is not valid or filled.
requiredFields.forEach((field) => {
    field.addEventListener("input", () => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);

        // Once field is filled remove red styling.
        if (!requiredFields.some((requiredField) => requiredField.classList.contains("invalid"))) {
            addPhoneStatus.textContent = "";
            addPhoneStatus.classList.remove("error");
        }
    });
});

// TODO: Add springboot logic to update phones accordingly.
addPhoneDialog.addEventListener("close", () => {
    if (addPhoneDialog.returnValue === "confirm") {
        addPhoneStatus.textContent = "Phone creation is not connected yet.";
        addPhoneStatus.classList.remove("error");
    }
});
