import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException{
        List<Phone> totalPhones = InventoryParser.parseInventory("data/cellphonedata.txt");
        System.out.println("Phone inventory loaded successfully. Full inventory:");
        printInventory(totalPhones);
        Scanner scanner = new Scanner(System.in);
        List<String> filters = new java.util.ArrayList<>();
        printCommands(filters);
        List<Phone> filteredPhones = totalPhones;
        while(scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }
            if(input.equals("")) {
                System.out.println("\nNo filter criteria provided. Full inventory:");
                printInventory(filteredPhones);
                printCommands(filters);
                continue;
            }
            if(input.equals("clear")) {
                filters.clear();
                System.out.println("\nFilters cleared. Full inventory:");
                filteredPhones = totalPhones;
                printInventory(filteredPhones);
                printCommands(filters);
                continue;
            }
            else{
                if(!InventorySort.validFilter(input)) {
                    printCommands(filters);
                    continue;
                }
                for(String filter : filters) {
                    if(filter.toLowerCase().equals(input.toLowerCase())) {
                        System.out.println("\nFilter criteria already applied. Please try again.");
                        printCommands(filters);
                        continue;
                    }
                }
                filters.add(input);
                filteredPhones = InventorySort.filterPhones(totalPhones, filters);
                if(filteredPhones.isEmpty()) {
                    System.out.println("\nNo phones match the current filter criteria.");
                }
                else {
                    System.out.println("\nFiltered inventory:");
                    printInventory(filteredPhones);
                }
                printCommands(filters);  
            }
        }
        scanner.close();

    }

    static void printCommands(List<String> filters) {
        System.out.println("--------------------------------------------------");
        if(filters.isEmpty()) {
            System.out.println("Current filters: None");
        } else {
            System.out.println("Current filters: " + String.join(", ", filters));
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Add filter criteria (brand, model, storage, color, or price) to find matching phones.");
        System.out.println("Example usage: \"Brand => Google\", \"Price => < $900\", \"Storage => 128GB\", \"Color => Black, Gray\"");
        System.out.println("Type \"clear\" to clear all filters, or type \"exit\" to quit the program.");
        System.out.print("\nInput: ");
    }

    static void printInventory(List<Phone> phones) {
        for (Phone phone : phones) {
            System.out.println(phone.getBrand() + " " + phone.getModel() + ", " + phone.getStorage() + ", " + phone.getColor() + ", $" + phone.getPrice());
        }
    }
}