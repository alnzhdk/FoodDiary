package models;

public class Product {
    private int id;
    private String name;
    private double calories;
    private double protein;
    private double fat;
    private double carbs;

    public Product(int id, String name, double calories, double protein, double fat, double carbs) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    @Override
    public String toString() {
        return name;
    }
}