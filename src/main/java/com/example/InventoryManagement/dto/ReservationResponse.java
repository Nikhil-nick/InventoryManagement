package com.example.InventoryManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long itemId;
    private int reservedQuantity;
    private String reservedBy;
    private Long reservationId;
}
