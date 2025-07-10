package com.example.InventoryManagement.exceptionHandling;

public class ReservationNotFoundException extends RuntimeException{
    public ReservationNotFoundException(Long id) {
        super("Reservation with ID " + id + " not found.");
    }
}
