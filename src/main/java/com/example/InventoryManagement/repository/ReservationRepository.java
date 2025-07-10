package com.example.InventoryManagement.repository;

import com.example.InventoryManagement.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findTopByItems_IdAndReservedByOrderByReservedAtDesc(Long itemId, String reservedBy);
}
