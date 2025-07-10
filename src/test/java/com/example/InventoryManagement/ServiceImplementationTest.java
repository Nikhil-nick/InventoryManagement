package com.example.InventoryManagement;

import com.example.InventoryManagement.entity.Items;
import com.example.InventoryManagement.entity.Reservation;
import com.example.InventoryManagement.entity.ReservationStatus;
import com.example.InventoryManagement.exceptionHandling.OutOfStockException;
import com.example.InventoryManagement.exceptionHandling.ReservationNotFoundException;
import com.example.InventoryManagement.repository.ItemRepository;
import com.example.InventoryManagement.repository.ReservationRepository;
import com.example.InventoryManagement.service.ServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServiceImplementationTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ServiceImplementation service;

    private Items item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = Items.builder()
                .id(1L)
                .name("iPhone")
                .quantity(10)
                .reservedQuantity(0)
                .price(999.0)
                .build();
    }

    @Test
    void testCreateItem_success() {
        when(itemRepository.save(any())).thenReturn(item);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Items saved = service.createItem(item);

        assertEquals("iPhone", saved.getName());
        verify(valueOperations).set("stock:1", 10);
    }

    @Test
    void testGetItemById_found() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Optional<Items> result = service.getItemById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testReserveItem_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("stock:1", 2)).thenReturn(8L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        Items result = service.reserveItem(1L, 2, "bharat@example.com");

        assertEquals(2, result.getReservedQuantity());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testReserveItem_outOfStock() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("stock:1", 2)).thenReturn(-1L);
        when(valueOperations.increment("stock:1", 2)).thenReturn(2L);

        assertThrows(OutOfStockException.class, () ->
                service.reserveItem(1L, 2, "bharat@example.com")
        );
    }

    @Test
    void testCancelReservation_success() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .items(item)
                .reservedQuantity(2)
                .reservedBy("bharat@example.com")
                .reservedAt(LocalDateTime.now())
                .status(ReservationStatus.RESERVED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(itemRepository.save(any())).thenReturn(item);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Items updated = service.cancelReservation(1L);

        assertEquals(-2, updated.getReservedQuantity());
        verify(valueOperations).increment("stock:1", 2);
    }

    @Test
    void testCancelReservation_notFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ReservationNotFoundException.class, () -> service.cancelReservation(999L));
    }

    @Test
    void testReserveItem_itemNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("stock:1", 2)).thenReturn(5L);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        when(valueOperations.increment("stock:1", 2)).thenReturn(7L);

        assertThrows(RuntimeException.class, () -> service.reserveItem(1L, 2, "test@example.com"));
    }
}
