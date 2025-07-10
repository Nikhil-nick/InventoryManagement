package com.example.InventoryManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Use object reference, not just ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Items items;

    private Integer reservedQuantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String reservedBy;

    private LocalDateTime reservedAt;
    private LocalDateTime cancelledAt;

    @PrePersist
    public void setReservationTimestamp() {
        this.reservedAt = LocalDateTime.now();
    }

}
