package com.example.InventoryManagement.service;

import com.example.InventoryManagement.entity.Items;
import com.example.InventoryManagement.entity.Reservation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface ServiceInterface {

    Items createItem(Items item);

    Optional<Items> getItemById(Long id);

    List<Items> getAllItems();

    Items reserveItem(Long itemId, int quantity, String reservedBy);

    Items cancelReservation(Long reservationId);
}
