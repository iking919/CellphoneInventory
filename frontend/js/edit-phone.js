/* 
    edit-phone.js contains logic to provide edits to existing inventory. This includes
    an edit button in manage inventory page with corresponding field editing in a popup.
    
    Spring boot is needed to properly connect backend logic.
*/

// Establish entry points for edit button and popup.
const editDialog = document.querySelector("#edit-confirmation");
const editForm = document.querySelector("#edit-phone-form");
const editStatus = document.querySelector("#edit-phone-status");
const cancelEditButton = document.querySelector("#cancel-edit");

// Set initial selected phone to null .
let selectedPhone = null;

// Handle edit button clicks.
document.addEventListener("click", (event) => {

    // Locate clicked edit button and ensure it's valid.
    if (!(event.target instanceof Element)) {
        return;
    }

    const editButton = event.target.closest(".edit-button");
    if (!editButton) {
        return;
    }

    // Obtain attributes of selected phone that may be editted.
    selectedPhone = {
        index: editButton.dataset.phoneIndex,
        brand: editButton.dataset.brand,
        model: editButton.dataset.model,
        storage: editButton.dataset.storage,
        color: editButton.dataset.color,
        price: editButton.dataset.price
    };

    // Add phone attributes to edit popup.
    editForm.elements.brand.value = selectedPhone.brand;
    editForm.elements.model.value = selectedPhone.model;
    editForm.elements.storage.value = selectedPhone.storage;
    editForm.elements.color.value = selectedPhone.color;
    editForm.elements.price.value = selectedPhone.price;
    editStatus.textContent = "";
    editStatus.classList.remove("error");
    
    // Show edit popup
    editDialog.showModal();
});

// Handle edit form submission.
editForm.addEventListener("submit", (event) => {
    
    // Prevent browser default form submission and page reload
    event.preventDefault();

    // Ensure that all fields are filled and valid.
    const requiredFields = Array.from(editForm.querySelectorAll("[required]"));
    let formIsValid = true;
    requiredFields.forEach((field) => {
        const fieldIsValid = field.value.trim() !== "" && field.checkValidity();
        field.classList.toggle("invalid", !fieldIsValid);
        if (!fieldIsValid) {
            formIsValid = false;
        }
    });

    // Display error if any fields aren't valid.
    if (!formIsValid) {
        editStatus.textContent = "Please fill in all fields with valid values.";
        editStatus.classList.add("error");
        return;
    }

    // Obtain new, editted phone values.
    const updatedPhone = {
        brand: editForm.elements.brand.value.trim(),
        model: editForm.elements.model.value.trim(),
        storage: editForm.elements.storage.value.trim(),
        color: editForm.elements.color.value.trim(),
        price: Number(editForm.elements.price.value)
    };

    // TODO: Add springboot logic to update phones accordingly.
    console.log("Updated phone:", selectedPhone.index, updatedPhone);
    editStatus.textContent = "Phone update is not connected to the server yet.";
    editStatus.classList.remove("error");
});


// Close the popup when edit is cancelled.
cancelEditButton.addEventListener("click", () => {
    editDialog.close();
});