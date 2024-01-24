package com.example.ict602project;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String itemName;
    private double itemPrice;
    private int quantity;

    // Required no-argument constructor
    public CartItem() {
        // Default constructor required for calls to DataSnapshot.getValue(CartItem.class)
    }

    public CartItem(String itemName, double itemPrice, int quantity) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return getItemPrice() * getQuantity();
    }
}
