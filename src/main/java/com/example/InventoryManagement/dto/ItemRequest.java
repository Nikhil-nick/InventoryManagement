package com.example.InventoryManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    @PositiveOrZero(message = "Quantity must be zero or more")
    private int quantity;

    @Positive(message = "Price must be greater than zero")
    private double price;

}
