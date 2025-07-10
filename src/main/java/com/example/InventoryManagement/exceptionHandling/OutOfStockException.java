package com.example.InventoryManagement.exceptionHandling;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(Long itemId) {
        super("Item with ID " + itemId + " is out of stock.");
    }
}
