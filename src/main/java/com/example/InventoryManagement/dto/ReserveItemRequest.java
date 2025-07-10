package com.example.InventoryManagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveItemRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "ReservedBy must not be blank")
    private String reservedBy;
}
