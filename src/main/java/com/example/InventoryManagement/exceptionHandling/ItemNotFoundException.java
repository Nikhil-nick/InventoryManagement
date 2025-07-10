package com.example.InventoryManagement.exceptionHandling;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long itemId) {
        super("Item with ID " + itemId + " not found.");
    }
}
