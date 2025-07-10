package com.example.InventoryManagement;

import com.example.InventoryManagement.dto.ReserveItemRequest;
import com.example.InventoryManagement.entity.Items;
import com.example.InventoryManagement.repository.ItemRepository;
import com.example.InventoryManagement.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long itemId;

    @BeforeEach
    void setupTestData() {

        // Save item in DB
        Items item = Items.builder()
                .name("MacBook Pro")
                .sku("MBP2024")
                .reservedQuantity(0)
                .price(200000.0)
                .build();

        Items saved = itemRepository.save(item);
        itemId = saved.getId();

        // Save stock in Redis
        redisTemplate.opsForValue().set("stock:" + itemId, saved.getQuantity());
    }

    @Test
    @DisplayName("POST /api/items/{id}/reserve - should reserve item and update Redis")
    void testReserveItemIntegration_success() throws Exception {
        // Arrange
        ReserveItemRequest request = new ReserveItemRequest(2, "bharat@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/items/{id}/reserve", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reservedQuantity").value(2));

        // Redis assertion
        Object redisValue = redisTemplate.opsForValue().get("stock:" + itemId);
        assertThat(redisValue)
                .as("Stock in Redis should be reduced by reserved quantity")
                .isEqualTo(8);
    }
}
