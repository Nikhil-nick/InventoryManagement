package com.example.InventoryManagement.repository;

import com.example.InventoryManagement.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Items,Long> {
}
