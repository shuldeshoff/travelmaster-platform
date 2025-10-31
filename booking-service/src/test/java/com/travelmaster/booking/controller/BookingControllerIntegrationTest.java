package com.travelmaster.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmaster.booking.dto.CreateBookingRequest;
import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import com.travelmaster.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("BookingController Integration Tests")
class BookingControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    private Booking testBooking;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();

        testBooking = Booking.builder()
                .bookingReference("BK-2025-TEST-001")
                .userId(100L)
                .tripId(200L)
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("50000.00"))
                .build();

        testBooking = bookingRepository.save(testBooking);
    }

    @Test
    @DisplayName("Should get booking by ID via API")
    void shouldGetBookingById() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBooking.getId()))
                .andExpect(jsonPath("$.bookingReference").value("BK-2025-TEST-001"))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.tripId").value(200))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(50000.00));
    }

    @Test
    @DisplayName("Should return 404 when booking not found")
    void shouldReturn404WhenBookingNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get bookings by user ID")
    void shouldGetBookingsByUserId() throws Exception {
        // Create additional booking for same user
        Booking booking2 = Booking.builder()
                .bookingReference("BK-2025-TEST-002")
                .userId(100L)
                .tripId(201L)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("60000.00"))
                .build();
        bookingRepository.save(booking2);

        mockMvc.perform(get("/api/v1/bookings/user/{userId}", 100L)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePagination() throws Exception {
        // Create multiple bookings
        for (int i = 0; i < 5; i++) {
            Booking booking = Booking.builder()
                    .bookingReference("BK-2025-TEST-" + (100 + i))
                    .userId(100L)
                    .tripId(200L + i)
                    .status(BookingStatus.PENDING)
                    .totalAmount(new BigDecimal("50000.00"))
                    .build();
            bookingRepository.save(booking);
        }

        mockMvc.perform(get("/api/v1/bookings/user/{userId}", 100L)
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.page").value(0));
    }
}

