package com.example.InventoryManagement.service;

import com.example.InventoryManagement.entity.Items;
import com.example.InventoryManagement.entity.Reservation;
import com.example.InventoryManagement.entity.ReservationStatus;
import com.example.InventoryManagement.exceptionHandling.ItemNotFoundException;
import com.example.InventoryManagement.exceptionHandling.OutOfStockException;
import com.example.InventoryManagement.exceptionHandling.ReservationNotFoundException;
import com.example.InventoryManagement.repository.ItemRepository;
import com.example.InventoryManagement.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceImplementation implements ServiceInterface {

    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @CacheEvict(value = "items", key = "#item.id")
    @Override
    public Items createItem(Items item) {
        if (item.getReservedQuantity() == null) {
            item.setReservedQuantity(0);
        }
        Items saved = itemRepository.save(item);
        redisTemplate.opsForValue().set("stock:" + saved.getId(), saved.getQuantity());
        return saved;
    }

    @Cacheable(value = "items", key = "#id")
    @Override
    public Optional<Items> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Items> getAllItems() {
        return itemRepository.findAll();
    }

    @Retryable(
            value = {DeadlockLoserDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @CacheEvict(value = "items", key = "#itemId")
    @Override
    @Transactional
    public Items reserveItem(Long itemId, int quantity, String reservedBy) {
        String redisKey = "stock:" + itemId;
        Long newStock = redisTemplate.opsForValue().decrement(redisKey, quantity);

        if (newStock == null) {
            throw new RuntimeException("Redis is unavailable or stock key is missing.");
        }

        if (newStock < 0) {
            redisTemplate.opsForValue().increment(redisKey, quantity);
            throw new OutOfStockException(itemId);
        }

        try {
            Items items = itemRepository.findById(itemId)
                    .orElseThrow(() -> new ItemNotFoundException(itemId));

            Reservation reservation = Reservation.builder()
                    .items(items)
                    .reservedQuantity(quantity)
                    .status(ReservationStatus.RESERVED)
                    .reservedBy(reservedBy)
                    .reservedAt(LocalDateTime.now())
                    .build();
            reservationRepository.save(reservation);

            items.setReservedQuantity(items.getReservedQuantity() + quantity);
            return itemRepository.save(items);

        } catch (Exception e) {
            redisTemplate.opsForValue().increment(redisKey, quantity);
            throw new RuntimeException("Reservation failed. Redis stock rolled back.", e);
        }
    }

    @CacheEvict(value = "items", key = "#reservationId")
    @Override
    @Transactional
    public Items cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Only RESERVED reservations can be cancelled.");
        }

        Items item = reservation.getItems();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        item.setReservedQuantity(item.getReservedQuantity() - reservation.getReservedQuantity());
        itemRepository.save(item);

        String redisKey = "stock:" + item.getId();
        redisTemplate.opsForValue().increment(redisKey, reservation.getReservedQuantity());

        return item;
    }
}
