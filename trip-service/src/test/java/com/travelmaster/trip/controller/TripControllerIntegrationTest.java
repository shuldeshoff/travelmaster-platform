package com.travelmaster.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmaster.trip.entity.Trip;
import com.travelmaster.trip.entity.TripStatus;
import com.travelmaster.trip.repository.TripRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("TripController Integration Tests")
class TripControllerIntegrationTest {

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
    private TripRepository tripRepository;

    private Trip testTrip;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        
        testTrip = Trip.builder()
                .title("Amazing Trip to Paris")
                .description("Experience the beauty of Paris")
                .origin("Moscow")
                .destination("Paris")
                .departureDate(LocalDateTime.now().plusDays(30))
                .returnDate(LocalDateTime.now().plusDays(37))
                .price(new BigDecimal("50000.00"))
                .currency("RUB")
                .availableSeats(20)
                .totalSeats(50)
                .status(TripStatus.AVAILABLE)
                .build();

        testTrip = tripRepository.save(testTrip);
    }

    @Test
    @DisplayName("Should get trip by ID via API")
    void shouldGetTripById() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", testTrip.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTrip.getId()))
                .andExpect(jsonPath("$.title").value("Amazing Trip to Paris"))
                .andExpect(jsonPath("$.origin").value("Moscow"))
                .andExpect(jsonPath("$.destination").value("Paris"))
                .andExpect(jsonPath("$.price").value(50000.00))
                .andExpect(jsonPath("$.availableSeats").value(20))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Should return 404 when trip not found")
    void shouldReturn404WhenTripNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search trips by origin and destination")
    void shouldSearchTrips() throws Exception {
        mockMvc.perform(get("/api/v1/trips/search")
                        .param("origin", "Moscow")
                        .param("destination", "Paris")
                        .param("departureDate", LocalDateTime.now().plusDays(30).toLocalDate().toString())
                        .param("passengers", "2")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].origin").value("Moscow"))
                .andExpect(jsonPath("$.content[0].destination").value("Paris"))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should get all available trips")
    void shouldGetAllTrips() throws Exception {
        mockMvc.perform(get("/api/v1/trips")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should filter trips with no results")
    void shouldReturnEmptyWhenNoTripsMatch() throws Exception {
        mockMvc.perform(get("/api/v1/trips/search")
                        .param("origin", "NonExistent")
                        .param("destination", "NoWhere")
                        .param("departureDate", LocalDateTime.now().plusDays(30).toLocalDate().toString())
                        .param("passengers", "2")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePagination() throws Exception {
        // Create multiple trips
        for (int i = 0; i < 5; i++) {
            Trip trip = Trip.builder()
                    .title("Trip " + i)
                    .origin("Moscow")
                    .destination("Paris")
                    .departureDate(LocalDateTime.now().plusDays(30 + i))
                    .price(new BigDecimal("50000.00"))
                    .currency("RUB")
                    .availableSeats(20)
                    .totalSeats(50)
                    .status(TripStatus.AVAILABLE)
                    .build();
            tripRepository.save(trip);
        }

        mockMvc.perform(get("/api/v1/trips")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.page").value(0));
    }
}

