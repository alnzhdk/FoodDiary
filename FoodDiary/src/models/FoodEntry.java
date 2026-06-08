package models;

import java.time.LocalDate;

public class FoodEntry {
    private int id;
    private LocalDate date;
    private Product product;
    private double grams;

    public FoodEntry(int id, LocalDate date, Product product, double grams) {
        this.id = id;
        this.date = date;
        this.product = product;
        this.grams = grams;
    }

    public double getTotalCalories() {
        if (product == null) return 0;
        return product.getCalories() * grams / 100;
    }

    public double getTotalProtein() {
        if (product == null) return 0;
        return product.getProtein() * grams / 100;
    }

    public double getTotalFat() {
        if (product == null) return 0;
        return product.getFat() * grams / 100;
    }

    public double getTotalCarbs() {
        if (product == null) return 0;
        return product.getCarbs() * grams / 100;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(double grams) {
        this.grams = grams;
    }
}