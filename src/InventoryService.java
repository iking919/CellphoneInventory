import java.util.List;

/* 
    InventoryService provides a static method to filter a list of phone objects based on a list of string filters, returning an ordered, filtered list.
    It also provides a static method to return a boolean if an inputted string filter is valid based upon a provided list of fields.
    A filter is considered valid if it involves one, and only one of the specified fields, and is in the correct form: "field => value(s)".
*/
public class InventoryService {

    // Initialize explict validFields list based on the pre-defined fields given.
    private static final List<String> validFields = List.of("brand", "model", "storage", "color", "price");

    // filterPhones method takes in a list of phones and a list of filter strings, and returns an ordered list of filtered phones.
    public static List<Phone> filterPhones(List<Phone> phones, List<String> filters) {

        // Create a duplicate list of phones which will be filtered accordingly.
        List<Phone> filteredPhones = new java.util.ArrayList<>(phones);

        // For each included filter, remove phones from the list of filteredPhones if they don't fit the filter.
        for (String filter : filters) {

            // Use matchesFilter function to determine phones that don't fit the specified filter, and remove it from the list accordingly.
            filteredPhones.removeIf(phone -> !matchesFilter(phone, filter));
        }

        // Once the list of phones has been filtered, use orderPhones to order the phones accordingly and return.
        return orderPhones(filteredPhones, filters);
    }

    // validFilter method takes in a filter string and returns a boolean if it uses a valid field, and is in valid format "field => value(s)".
    static boolean validFilter(String filter) {

        // If filter doesn't contain "=>" it is invalid. Output corresponding message and return false.
        if(!filter.contains("=>")) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field => value'.");
            return false;
        }

        // Split the filter string along "=>", extracting the field (arg[0]) and value (arg[1]) arguments.
        String[] args = filter.split("=>", -1);

        // If argument length > 2, the filter string contained 2 "=>" and is invalid. Output corresponding message and return false.
        if(args.length != 2) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field=>value'.");
            return false;
        }

        // If either the field or values arguments are empty, filter is invalid. Output corresponding message and return false.
        if(args[0].strip().isEmpty() || args[1].strip().isEmpty()) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field=>value'.");
            return false;
        }

        // Split values argument among "," accounting for instances if a filter is being evaluated on multiple values.
        String[] values = args[1].split(",", -1);

        // For each value in the filter field determine if it is empty. If any are empty, the filter is invalid. 
        // Output corresponding message and return false.
        for(String value : values) {
            if(value.strip().isEmpty()) {
                System.out.println("\nInvalid filter format: values cannot be empty.");
                return false;
            }
        }
        
        // If the filter field is "price", explicitly ensure that the corresponding field is valid (Contains a double, optionally with $).
        if(args[0].strip().toLowerCase().equals("price")){

            // For each value in the values field call isValidPrice method to determine if it is in valid price format, returning false if it's not.
            for(String value : values) {
                if(!isValidPrice(value.strip())) {
                    return false;
                }
            }
        }

        // If the filter field is "storage", explicitly ensure that the corresponding field is valid (Contains an integer, followed by "GB" or "TB").
        if(args[0].strip().toLowerCase().equals("storage")){

            // For each value in the values field call isValidStorage method to determine if it is in valid storage format, returning false if it's not.
            for(String value : values) {
                if(!isValidStorage(value.strip())) {
                    return false;
                }
            }
        }

        // Finally, if all filter values are valid, return true if filter's field is also valid, otherwise return false.
        return validFields.contains(args[0].toLowerCase().strip());
    }

    // matchesFilter method takes a phone object and a filter string and returns true if the phone fits the filter criteria, and false if not.
    private static boolean matchesFilter(Phone phone, String filter) {

        // Split filter string along "=>" to get field and value arguments.
        String[] args = filter.split("=>", -1);
        String field = args[0].toLowerCase().strip();
        String values = args[1].toLowerCase().strip();

        // If the field is "brand" we can compare the brand of the phone to the brand(s) in the values argument.
        if(field.equals("brand")) {

            // Split values by "," to extract each brand if multiple are present.
            for(String brand : values.split(",")) {
                
                // If the phone object's brand matches any of the criteria brands, return true.
                if(phone.getBrand().toLowerCase().equals(brand.strip())) {
                    return true;
                }
            }

        // If the field is "model" we can compare the model of the phone to the model(s) in the values argument.
        } else if(field.equals("model")) {

            // Split values by "," to extract each model if multiple are present.
            for(String model : values.split(",")) {

                // If the phone object's model matches any of the criteria models, return true.
                if(phone.getModel().toLowerCase().contains(model.strip())) {
                    return true;
                }
            }

        // If the field is "storage" we compare the storage of the phone to the storage(s) in the values argument.
        } else if(field.equals("storage")) {

            // Create boolean to track if phone object violates any storage filter criteria.
            boolean validStorage = true;

            // Split values by "," to extract each storage value if multiple are present, iterating through each storage value.
            for(String storage : values.split(",")) {

                // Use the following regex to split filter storage along ">", "<", "=", and when the string switches from integers to constants.
                // For example, we can split "> 128GB" into [">","128","GB"] and "1TB" into ["1","TB"].
                String[] storageValues = storage.strip().toLowerCase().split("(?<=\\d)\\s*(?=[a-zA-Z])|\\s+|(?=[<>=])|(?<=[<>=])");

                // Use the same regex to split phone storage when string switches from integers to constants.
                String[] phoneStorageValues = phone.getStorage().toLowerCase().split("(?<=\\d)\\s*(?=[a-zA-Z])|\\s+|(?=[<>=])|(?<=[<>=])");
                
                // If storageValues has a length of 3, it contains a comparison operator (">", "<", "=").
                if(storageValues.length == 3){

                    // Call storageInGB function to have equal units for comparison, regardless if filter or phone storage is in terms of TB.
                    long phoneCapacity = storageInGB(phoneStorageValues[0], phoneStorageValues[1]);
                    long filterCapacity = storageInGB(storageValues[1], storageValues[2]);

                    // Make comparisons based on value of comparison operator in storageValues[0]. 
                    // If the phone doesn't match the criteria update boolean value.
                    String operator = storageValues[0].strip();
                    if(!((operator.equals("<") && phoneCapacity < filterCapacity)
                            || (operator.equals(">") && phoneCapacity > filterCapacity)
                            || (operator.equals("=") && phoneCapacity == filterCapacity))) {
                        validStorage = false;
                    }

                // If storageValues does not have a length of 3, assume criteria is an equals comparison. 
                // Thus we compare criteria and phone storage values and units, updating boolean value if phone isn't equal to criteria.
                } else if(storageInGB(storageValues[0], storageValues[1]) != storageInGB(phoneStorageValues[0], phoneStorageValues[1])) {
                    validStorage = false;
                }
            }

            // After making all filter comparisons, return the tracked boolean value.
            return validStorage;
        
        // If the field is "color" we compare the color of the phone to the color(s) in the values argument.
        } else if(field.equals("color")) {

            // Split values by "," to extract each color if multiple are present.
            for(String color : values.split(",")) {

                // If the phone object's color matches any of the criteria colors, return true.
                if(phone.getColor().toLowerCase().contains(color.strip())) {
                    return true;
                }
            }

        // If the field is "price" we compare the price of the phone to the prices(s) in the values argument.
        } else if(field.equals("price")) {

            // Create a boolean to track if the phone object violates any filter criteria.
            boolean validPrice = true;

            // Split values by "," to extract each price if multiple are present.
            for(String price : values.split(",")) {

                // If the value field contains the "<", compare the phone price and filter price accordingly.
                if ( price.contains("<")) {

                    // Replace "<" and "$" using parseDouble to get filter price as a double.
                    double amount = Double.parseDouble(price.replace("<", "").replace("$", ""));
                    
                    // Make comparison, if filter criteria isn't met update boolean value accordingly.
                    if(phone.getPrice() > amount) {
                        validPrice = false;
                    }
            
                // If the value field contains the ">", compare the phone price and filter price accordingly.
                } else if ( price.contains(">")) {

                    // Replace ">" and "$" using parseDouble to get filter price as a double.
                    double amount = Double.parseDouble(price.replace(">", "").replace("$", ""));
                    
                    // Make comparison, if filter criteria isn't met update boolean value accordingly.
                    if(phone.getPrice() < amount) {
                        validPrice = false;;
                    }

                // If the value field contains the "=", compare the phone price and filter accordingly.
                } else if( price.contains("=")) {

                    // Replace "=" and "$" using parseDouble to get filter price as a double.
                    double amount = Double.parseDouble(price.replace("=", "").replace("$", ""));
                    
                    // Make comparison, if filter criteria isn't met update boolean value accordingly.
                    if(phone.getPrice() != amount) {
                        validPrice = false;
                    }

                // If the value field doesn't contain any operator treat the comparison as an "=" operation.
                } else{

                    // Replace "$" using parseDouble to get filter price as a double.
                    double amount = Double.parseDouble(price.replace("$", ""));

                    // Make comparison, if filter criteria isn't met update boolean value accordingly.
                    if(phone.getPrice() != amount) {
                        validPrice = false;
                    }
                }
            }

            // After making all filter comparisons, return the tracked boolean value.
            return validPrice;
        }

        // If no valid fields are found in the filter string return false. 
        // This should never happen, since all filters are checked to be valid.
        return false;
    }

    // storageInGB method takes a string storage amount and a string storage unit and returns the storage in GB as a long.
    private static long storageInGB(String amount, String unit) {

        // Use parseLong to get the storage amount as a long value.
        long amountGB = Long.parseLong(amount.strip());

        // If the storage unit is "tb", multiply the total storage amount by 1000.
        if(unit.strip().equals("tb")) {
            return amountGB * 1000;
        }

        // Return the converted storage amount.
        return amountGB;
    }

    // isValidPrice method takes a price filter string and returns a boolean if it is in valid format (i.e., "= $900", "< $900", or simply "$900").
    private static boolean isValidPrice(String price) {

        // Use a matches call to make sure the price has only one of "<", ">", "=", and only one "$".
        // The matches call also ensures the price is a whole number, followed optionally by a decimal.
        if(price.matches("(?:[<>]|=)?\\s*\\$?\\s*\\d+(?:\\.\\d+)?")) {

            // Remove any instances of "<", ">", "=" and "$", leaving just the numeric amount.
            String amount = price.replace("<", "").replace(">", "").replace("=", "").replace("$", "").strip();
            
            // If the final price amount is a valid finite double, return true.
            if(Double.isFinite(Double.parseDouble(amount))) {
                return true;
            }
        }

        // If the price filter doesn't match the provided regex, or doesn't contain a valid finite double return false with a corresponding output.
        System.out.println("\nInvalid price filter: \"" + price + "\"");
        return false;
    }

    // isValidPrice method takes a storage filter string and returns a boolean if it is in valid format (i.e., "= 1TB", "< 512GB", or simply "256GB").
    private static boolean isValidStorage(String storage) {

        // Use the following regex to split filter storage along ">", "<", "=", and when the string switches from integers to constants.
        // For example, we can split "> 128GB" into [">","128","GB"] and "1TB" into ["1","TB"].
        String[] storageValues = storage.toLowerCase().split(("(?<=\\d)\\s*(?=[a-zA-Z])|\\s+|(?=[<>=])|(?<=[<>=])"));

        // If storageValues has more than 3 or less than 2 arguments, it is not in proper format and is not valid. Return false and output accordingly.
        if(storageValues.length != 3 && storageValues.length != 2) {
            System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
            return false;
        }

        // Use a try block to catch any instances where a number format exception is thrown due to conversion errors.
        try {

            // If storageValues length is 3, it should contain a comparison operator ">", "<", "=".
            if(storageValues.length == 3) {

                // If the first storageValues argument is not a valid comparison operator, return false and output accordingly.
                if(!storageValues[0].strip().equals("<")
                        && !storageValues[0].strip().equals(">")
                        && !storageValues[0].strip().equals("=")) {
                    System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
                    return false;
                }

                // If the storage amount value is not a postive integer, the storage filter is invalid, return false and output accordingly.
                if(Integer.parseInt(storageValues[1].strip()) < 0) {
                    System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
                    return false;
                }

                // If the storage unit is not either "GB" or "TB" (regardless of case), the storage filter is invalid, return false and output accordingly.
                if(!storageValues[2].strip().equals("gb") && !storageValues[2].strip().equals("tb")) {
                    System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
                    return false;
                }

                // If the comparison operator, storage amount and storage unit are all valid, return true.
                return true;

            // If storageValues is not of length 3, it is assumed to have the "=" operator.
            } else{

                // If the storage amount value is not a postive integer, the storage filter is invalid, return false and output accordingly.
                if(Integer.parseInt(storageValues[0].strip()) < 0) {
                    System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
                    return false;
                }

                // If the storage unit is not either "GB" or "TB" (regardless of case), the storage filter is invalid, return false and output accordingly.
                if(!storageValues[1].strip().equals("gb") && !storageValues[1].strip().equals("tb")) {
                    System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
                    return false;
                }

                // If the comparison operator, storage amount and storage unit are all valid, return true.
                return true;
            }

        // If any number format exceptions were thrown during conversion, the storage filter is invalid, return false and output accordingly.
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid storage filter: " + "\"" + storage + "\"");
            return false;
        }
    }

    // orderPhones method takes in a list of phones and a list of filters, and returns and ordered list of phones.
    /*
        ****Ordering Logic:** Results are sorted contextually based on the search criteria. 
        When a price filter is applied, results are sorted by ascending price, followed by brand, model, storage and color alphabetically.
        Otherwise, results are sorted alphabetically by brand, color, model, and storage, with price used as the final tie-breaker. 
        This can be updated according to further specification****
    */
    private static List<Phone> orderPhones(List<Phone> phones, List<String> filters) {

        // If the filter list contains price, sort by price ascending, then brand,model, storage and color alphabetically.
        if(containsPriceFilter(filters)) {
            phones.sort(java.util.Comparator.comparing(Phone::getPrice)
                .thenComparing(Phone::getBrand)
                .thenComparing(Phone::getModel)
                .thenComparing(Phone::getStorage)
                .thenComparing(Phone::getColor));

        // Otherwise, results are sorted alphabetically by brand, color, model, and storage, with price used as the final tie-breaker. 
        } else {
            phones.sort(java.util.Comparator.comparing(Phone::getBrand)
                .thenComparing(Phone::getColor)
                .thenComparing(Phone::getModel)
                .thenComparing(Phone::getStorage)
                .thenComparing(Phone::getPrice));
        }

        // Return sorted phones.
        return phones;
    }

    // containsPriceFilter method takes in a list of filters and returns true if price is one of the filter fields.
    private static boolean containsPriceFilter(List<String> filters) {

        // Loop through each filter in the list of filters.
        for(String filter : filters) {

            // Split each filter along "=>"  to get the field and value arguments.
            String[] args = filter.split("=>", 2);

            // If the field argument is "price" regardless of case, return true.
            if(args[0].strip().equalsIgnoreCase("price")) {
                return true;
            }
        }

        // Otherwise, price is not a present filter. Therefore, return false.
        return false;
    }
}
