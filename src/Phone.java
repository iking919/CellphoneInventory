public class Phone {
    private String brand;
    private String model;
    private String storage;
    private String color;
    private Double price;

    public Phone(String brand, String model, String storage, String color, Double price) {
        this.brand = brand;
        this.model = model;
        this.storage = storage;
        this.color = color;
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getStorage() {
        return storage;
    }

    public String getColor() {
        return color;
    }

    public Double getPrice() {
        return price;
    }
}