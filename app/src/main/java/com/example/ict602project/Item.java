package com.example.ict602project;

public class Item {
    private int barcode;
    private String itemName;
    private double itemPrice;

    public Item() {

    }

    public Item(int barcode, String itemName, double itemPrice) {
        this.barcode = barcode;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public int getBarcode() {
        return barcode;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }
}
