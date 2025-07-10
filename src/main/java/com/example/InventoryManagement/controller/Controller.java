package com.example.InventoryManagement.controller;

import com.example.InventoryManagement.dto.ItemRequest;
import com.example.InventoryManagement.dto.ItemResponse;
import com.example.InventoryManagement.dto.ReservationResponse;
import com.example.InventoryManagement.dto.ReserveItemRequest;
import com.example.InventoryManagement.entity.Items;
import com.example.InventoryManagement.entity.Reservation;
import com.example.InventoryManagement.exceptionHandling.ItemNotFoundException;
import com.example.InventoryManagement.repository.ReservationRepository;
import com.example.InventoryManagement.service.ServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item API", description = "Inventory and reservation management endpoints")
public class Controller {

    private final ServiceInterface serviceInterface;
    private final ReservationRepository reservationRepository;

    @GetMapping("/ping")
    @Cacheable("ping")
    @Operation(summary = "Ping Redis", description = "Tests Redis caching by returning a static string with log on cache miss.")
    public String ping() {
        System.out.println("HIT");
        return "pong";
    }

    @GetMapping("/home")
    @Operation(summary = "Welcome message", description = "Test if the controller is reachable.")
    public ResponseEntity<String> welCome() {
        return ResponseEntity.ok("Welcome to Inventory management Project !!");
    }

    @PostMapping
    @Operation(summary = "Create Item", description = "Adds a new item to the inventory.")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        Items item = new Items();
        item.setName(request.getName());
        item.setSku(request.getSku());
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPrice());
        item.setReservedQuantity(0); // Default

        Items savedItem = serviceInterface.createItem(item);
        return ResponseEntity.ok(toItemResponse(savedItem));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Item by ID", description = "Fetch a specific item using its ID.")
    public ResponseEntity<ItemResponse> getItemsById(@PathVariable Long id) {
        return serviceInterface.getItemById(id)
                .map(this::toItemResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @GetMapping
    @Operation(summary = "Get All Items", description = "Returns all available items from the inventory.")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        List<ItemResponse> list = serviceInterface.getAllItems()
                .stream().map(this::toItemResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{itemId}/reserve")
    @Operation(summary = "Reserve Item", description = "Reserves quantity of an item for a specific user.")
    public ResponseEntity<ReservationResponse> reserveItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ReserveItemRequest reserveItemRequest) {

        Items item = serviceInterface.reserveItem(
                itemId,
                reserveItemRequest.getQuantity(),
                reserveItemRequest.getReservedBy()
        );

        Reservation reservation = reservationRepository
                .findTopByItems_IdAndReservedByOrderByReservedAtDesc(itemId, reserveItemRequest.getReservedBy())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        return ResponseEntity.ok(
                ReservationResponse.builder()
                        .itemId(item.getId())
                        .reservedQuantity(reservation.getReservedQuantity())
                        .reservedBy(reservation.getReservedBy())
                        .reservationId(reservation.getId())
                        .build()
        );
    }

    @PostMapping("/reservation/{reservationId}/cancel")
    @Operation(summary = "Cancel Reservation", description = "Cancels a reservation using its ID and updates inventory.")
    public ResponseEntity<ItemResponse> cancelReservation(@PathVariable Long reservationId) {
        Items items = serviceInterface.cancelReservation(reservationId);
        return ResponseEntity.ok(toItemResponse(items));
    }

    private ItemResponse toItemResponse(Items item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .price(item.getPrice())
                .build();
    }
}
