import java.io.IOException;
import java.util.List;
import java.util.Scanner;


/*
    The Main class serves as the entry point for the application.
    It initializes the phone inventory, handles the user's interaction, and coordinates the filtering and display of inventory results.
*/
public class Main {
    
    // Main method handles the user's inputs and the corresponding outputs, calling helper functions as needed.
    // User inputs commands. Commands include:
    //  - Filters they would like to apply
    //  - "clear" if they would like to clear all current filters
    //  - "confirm" if they would like to confirm filters and retrieve filtered phones
    //  - "exit" if they would like to exit the program
    public static void main(String[] args) {
        List<Phone> totalPhones;

        // Define the specified dataFilePath.
        String dataFilePath = "data/cellphonedata.txt";

        // Call the InventoryParser parseInventory method to parse the phone inventory.
        // If any errors occur during reading or loading the inventory file, output the corresponding error and end execution.
        try {
            totalPhones = InventoryParser.parseInventory(dataFilePath);
        } catch (IOException e) {
            System.err.println("ERROR: Could not read the inventory file: " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Could not load the inventory file: " + e.getMessage());
            return;
        }

        // If the total phone inventory is empty end execution and output accordingly.
        if(totalPhones.isEmpty()) {
            System.out.println("No valid phones found in the inventory file. Exiting program.");
            return;
        }

        // Print total phone inventory once loaded successfully.
        System.out.println("Phone inventory loaded successfully. Full inventory:");
        printInventory(totalPhones);

        // Initialize scanner to read in user inputs.
        Scanner scanner = new Scanner(System.in);

        // Initialize filter list to track applied filters.
        List<String> filters = new java.util.ArrayList<>();

        // Print user commands with the current (empty) filter list.
        printCommands(filters);

        // Initialize separate list of filtered phones, which will be updated accordingly as filters are applied.
        List<Phone> filteredPhones = totalPhones;

        // Continue application as long as there's a line of text for the scanner to read.
        while(scanner.hasNextLine()) {

            // Read in user's input.
            String input = scanner.nextLine();
            input = input.strip();

            // If user's input is "exit" break the while loop, and end the program.
            if (input.toLowerCase().equals("exit")) {
                break;

            // If user's input is blank print warning and print commands with current applied filters.
            } else if(input.equals("")) {
                System.out.println("\nNo filter criteria provided.");
                printCommands(filters);
                continue;

            // If user's input is "clear" clear the list of filters, reset the list of filtered phones, 
            // print the full inventory and print commands with current (empty) filter list.
            } else if(input.toLowerCase().equals("clear")) {
                filters.clear();
                System.out.println("\nFilters cleared. Full inventory:");
                filteredPhones = totalPhones;
                printInventory(filteredPhones);
                printCommands(filters);
                continue;

            // If user's input is "confirm" print the list of filtered phones accordingly,
            } else if(input.toLowerCase().equals("confirm")) {
                System.out.println("\nFilters confirmed.");

                // If filtered phones list is empty, output accordingly and print commands with current filters.
                if(filteredPhones.isEmpty()) {
                    System.out.println("No phones match the current filter criteria. Please try again.");
                    printCommands(filters);
                
                // If filtered inventory list isn't empty, print filtered inventory, along with commands and current filters.
                } else {
                    System.out.println("\nFiltered inventory:");
                    printInventory(filteredPhones);
                    printCommands(filters);
                }
                continue;

            // If user input isn't "exit", "clear", "confirm" or empty, it is assumed to be a filter.
            } else{

                // Use InventoryService validFilter method to ensure filter is valid given phone fields. 
                // If the filter isn't valid, it isn't added to the filter list, and command and current filters are printed.
                if(!InventoryService.validFilter(input)) {
                    printCommands(filters);
                    continue;
                }

                // Initialize boolean values to track if inputted filter should be added, and if it already replaced a previous filter in place.
                boolean addFilter = true;
                boolean filterReplaced = false;

                // Loop through each filter already applied, comparing it to user's input.
                for(String filter : filters) {

                    // If the user's inputted matches a filter that has already been applied exactly, output accordingly and do not re-add duplicate filter.
                    if(filter.split("=>")[0].toLowerCase().strip().equals(input.split("=>")[0].toLowerCase().strip())
                        && filter.split("=>")[1].toLowerCase().strip().equals(input.split("=>")[1].toLowerCase().strip())) {
                        System.out.println("\nFilter criteria already applied. Please try again.");
                        printCommands(filters);
                        addFilter = false;
                        break;
                    
                    // If user's inputted filter's field matches a current filter's field, give the user an option to replace the current filter.
                    // I.e., if "Price => > $900" is applied and "Price => < $900" is inputted, ask user to replace ("Y") existing filter or to leave it ("N").
                    } else if(filter.split("=>")[0].toLowerCase().strip().equals(input.split("=>")[0].toLowerCase().strip())) {
                        System.out.println("\nFilter criteria for this field already applied. Press \"Y\" to replace the existing filter, or \"N\" to keep the existing filter.");
                        
                        // Read in user's decision to replace filter criteria.
                        System.out.print("\nInput: ");
                        String response = scanner.nextLine().strip();

                        // If user inputs "Y" replace existing filter with new filter input, updating boolean values to avoid adding filter twice.
                        if(response.toLowerCase().equals("y")) {
                            filters.set(filters.indexOf(filter), input);
                            addFilter = false;
                            filterReplaced = true;
                            break;
                        
                        // If user inputs "N" ignore filter input, updating boolean values to not add filter input to set.
                        } else if(response.toLowerCase().equals("n")) {
                            printCommands(filters);
                            addFilter = false;
                            break;

                        // If user input isn't "Y" or "N" output invalid response warning, update boolean to not add filter input to set.
                        } else {
                            System.out.println("\nInvalid response. Please try again.");
                            printCommands(filters);
                            addFilter = false;
                            break;
                        }
                    }
                }

                // If booleans values indicate filter input shouldn't be added skip. This accounts for inputs that shouldn't be added, 
                // or those that replaced previous filters in place.
                if(!addFilter && !filterReplaced) {
                    continue;
                }

                // If boolean value indicates input should be added, ensure no more than 5 filters are already added, and add accordingly.
                if(addFilter) {

                    // If 5 filters are already added, there is one filter for each field, and no more should be added.
                    if(filters.size() >= 5) {
                        System.out.println("\nMaximum number of filters reached. Please clear filters before adding new ones.");
                        printCommands(filters);
                        continue;
                    }
                    filters.add(input);
                }

                // Following user inputs, filter phones with updated filter list, and print commands with updated filter list.
                filteredPhones = InventoryService.filterPhones(totalPhones, filters);
                printCommands(filters);  
            }
        }

        // Following the completion of the inventory search, close the scanner and terminate the program
        scanner.close();
    }

    // printCommands method takes a list of filters and outputs command instructions for users.
    static void printCommands(List<String> filters) {
        System.out.println("--------------------------------------------------");
        
        // Call printFilters method to print list of filters.
        printFilters(filters);
        System.out.println("--------------------------------------------------");

        // Print command instructions for user.
        System.out.println("Add filter criteria (brand, model, storage, color, or price) to find matching phones.");
        System.out.println("Example usage: \"Brand => Google\", \"Price => < $900\", \"Storage => 128GB\", \"Color => Black, Gray\"");
        System.out.println("Type \"confirm\" to apply the current filters, \"clear\" to clear all filters, or type \"exit\" to quit the program.");
        System.out.print("\nInput: ");
    }

    // printInventory method takes in a list of phone objects and prints all phone attributes, outputting an entire inventory list.
    static void printInventory(List<Phone> phones) {
        for (Phone phone : phones) {
            System.out.println(phone.getBrand() + " " + phone.getModel() + ", " + phone.getStorage() + ", " + phone.getColor() + ", $" + phone.getPrice());
        }
    }

    // printFilters method takes in a list of filter strings and output all filters accordingly, handling instances where filters list is empty.
    static void printFilters(List<String> filters) {
        if(filters.isEmpty()) {
            System.out.println("Current filters: None");
        } else {
            System.out.println("Current filters: " + String.join(", ", filters));
        }
    }
}