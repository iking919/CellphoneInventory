import java.util.List;

public class InventorySort {
    private static final List<String> validFields = List.of("brand", "model", "storage", "color", "price");

    public static List<Phone> filterPhones(List<Phone> phones, List<String> filters) {
        List<Phone> filteredPhones = new java.util.ArrayList<>(phones);
        for (String filter : filters) {
            filteredPhones.removeIf(phone -> !matchesFilter(phone, filter));
        }
        return filteredPhones;
    }

    static boolean validFilter(String filter) {
        if(!filter.contains("=>")) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field=>value'.");
            return false;
        }
        String[] args = filter.split("=>");
        if(args.length != 2) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field=>value'.");
            return false;
        }
        if(args[0].strip().isEmpty() || args[1].strip().isEmpty()) {
            System.out.println("\nInvalid filter format. Filter must be in the format 'field=>value'.");
            return false;
        }
        if(args[0].strip().toLowerCase().equals("price")){
            String[] priceArgs = args[1].split(",");
            for(String priceArg : priceArgs) {
                if(!isValidPrice(priceArg.strip())) {
                    return false;
                }
            }
        }
        return validFields.contains(args[0].toLowerCase().strip());
    }

    static String[] parseFilter(String filter) {
        return filter.split("=>");
    }

    private static boolean matchesFilter(Phone phone, String filter) {
        String[] args = parseFilter(filter);
        String field = args[0].toLowerCase().strip();
        String values = args[1].toLowerCase().strip();

        if(field.equals("brand")) {
            for(String brand : values.split(",")) {
                if(phone.getBrand().toLowerCase().equals(brand.strip())) {
                    return true;
                }
            }
        } else if(field.equals("model")) {
            for(String model : values.split(",")) {
                if(phone.getModel().toLowerCase().equals(model.strip())) {
                    return true;
                }
            }
        } else if(field.equals("storage")) {
            for(String storage : values.split(",")) {
                if(phone.getStorage().toLowerCase().equals(storage.strip())) {
                    return true;
                }
            }
        } else if(field.equals("color")) {
            for(String color : values.split(",")) {
                if(phone.getColor().toLowerCase().equals(color.strip())) {
                    return true;
                }
            }
        } else if(field.equals("price")) {
            for(String price : values.split(",")) {
                if ( price.contains("<")) {
                    double amount = Double.parseDouble(price.replace("<", "").replace("$", ""));
                    if(phone.getPrice() < amount) {
                        return true;
                    }
                } else if ( price.contains(">")) {
                    double amount = Double.parseDouble(price.replace(">", "").replace("$", ""));
                    if(phone.getPrice() > amount) {
                        return true;
                    }
                } else if( price.contains("=")) {
                    double amount = Double.parseDouble(price.replace("=", "").replace("$", ""));
                    if(phone.getPrice() == amount) {
                        return true;
                    }
                }
                else{
                    double amount = Double.parseDouble(price.replace("$", ""));
                    if(phone.getPrice() == amount) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isValidPrice(String price) {
        if (price.contains("<")) {
            String amount = price.replace("<", "").replace("$", "").strip();
            try {
                Double.parseDouble(amount);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid price filter: " + "\"" + amount + "\"");
                return false;
            }
        } else if (price.contains(">")) {
            String amount = price.replace(">", "").replace("$", "").strip();
            try {
                Double.parseDouble(amount);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid price filter: " + "\"" + amount + "\"");
                return false;
            }
        } else if (price.contains("=")) {
            String amount = price.replace("=", "").replace("$", "").strip();
            try {
                Double.parseDouble(amount);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid price filter: " + "\"" + amount + "\"");
                return false;
            }
        } else {
            String amount = price.replace("$", "").strip();
            try {
                Double.parseDouble(amount);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid price filter: " + "\"" + amount + "\"");
                return false;
            }
        }
    }
}
