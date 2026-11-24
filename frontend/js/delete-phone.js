/*
    delete-phone.js contains logic to delete existing inventory. This includes
    an delete button in manage inventory page with corresponding confirmation popup.
    
    Spring boot is needed to properly connect backend logic.
*/

// Establish entry points for delete button and popup.
const deleteDialog = document.querySelector("#delete-confirmation");
const deleteForm = document.querySelector("#delete-form");
const cancelDeleteButton = document.querySelector("#cancel-delete");
const deleteStatusMessage = document.querySelector("#inventory-status");
const deleteConfirmationMessage = document.querySelector("#delete-confirmation-message");

// Handle delete button clicks.
document.addEventListener("click", (event) => {

    // Locate clicked delete button and ensure it's valid.
    if (!(event.target instanceof Element)) {
        return;
    }

    const deleteButton = event.target.closest(".delete-button");
    if (!deleteButton) {
        return;
    }

    // Obtain selected phone attributes to provide context in deletion popup message.
    const { brand, model, storage, color, price } = deleteButton.dataset;
    deleteConfirmationMessage.textContent =
        `Are you sure you want to delete the ${brand} ${model} (${storage}, ${color}, ${price})?`;
    deleteDialog.showModal();
});


// Handle delete form submission.
// TODO: Add springboot logic to delete phone accordingly.
deleteForm.addEventListener("submit", (event) => {
    
    // Prevent browser default form submission and page reload
    event.preventDefault();
    deleteStatusMessage.textContent = "Deletion is not connected yet.";
    deleteDialog.close();
});

// Close the popup when deletion is cancelled.
cancelDeleteButton.addEventListener("click", () => {
    deleteDialog.close();
});
