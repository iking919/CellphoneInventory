// TODO: Connect confirmed deletions to the Spring Boot delete endpoint.
const deleteDialog = document.querySelector("#delete-confirmation");
const deleteStatusMessage = document.querySelector("#inventory-status");

document.addEventListener("click", (event) => {
    if (!(event.target instanceof Element)) {
        return;
    }

    const deleteButton = event.target.closest(".delete-button");
    if (!deleteButton) {
        return;
    }

    deleteDialog.showModal();
});

deleteDialog.addEventListener("close", () => {
    if (deleteDialog.returnValue === "confirm") {
        deleteStatusMessage.textContent = "Deletion is not connected yet.";
    }
});
