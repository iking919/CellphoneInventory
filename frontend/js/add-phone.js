const addPhoneForm = document.querySelector("#add-phone-form");
const addPhoneStatus = document.querySelector("#add-phone-status");
const addPhoneDialog = document.querySelector("#add-confirmation");
const priceInput = document.querySelector("#price");
const currencyInput = priceInput.parentElement;
const requiredFields = Array.from(addPhoneForm.querySelectorAll("[required]"));

// Use has-value to add "$" to price input.
function updatePriceSymbol() {
    currencyInput.classList.toggle("has-value", priceInput.value !== "");
}

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

// Prevent browser default form submission and page reload, opening custom confirmation.
addPhoneForm.addEventListener("submit", (event) => {
    event.preventDefault();
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

// TEMPORARY output phone creation isn't connected since logic isn't added
addPhoneDialog.addEventListener("close", () => {
    if (addPhoneDialog.returnValue === "confirm") {
        addPhoneStatus.textContent = "Phone creation is not connected yet.";
        addPhoneStatus.classList.remove("error");
    }
});


// Run updatePriceSymbol whenever price field changes.
priceInput.addEventListener("input", updatePriceSymbol);
updatePriceSymbol();
