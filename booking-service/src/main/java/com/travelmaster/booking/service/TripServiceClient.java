package com.travelmaster.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * HTTP клиент для взаимодействия с Trip Service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripServiceClient {

    private final RestTemplate restTemplate;
    private static final String TRIP_SERVICE_URL = "http://trip-service:8082/api/v1/trips";

    /**
     * Получить информацию о поездке.
     */
    public Optional<TripResponse> getTripById(Long tripId) {
        try {
            log.info("Fetching trip info for trip: {}", tripId);
            TripResponse response = restTemplate.getForObject(
                    TRIP_SERVICE_URL + "/" + tripId,
                    TripResponse.class
            );
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Error fetching trip {}: {}", tripId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Зарезервировать места.
     */
    public boolean reserveSeats(Long tripId, int numberOfSeats) {
        try {
            log.info("Reserving {} seats for trip {}", numberOfSeats, tripId);
            restTemplate.postForObject(
                    TRIP_SERVICE_URL + "/" + tripId + "/reserve?seats=" + numberOfSeats,
                    null,
                    Void.class
            );
            return true;
        } catch (Exception e) {
            log.error("Error reserving seats for trip {}: {}", tripId, e.getMessage());
            return false;
        }
    }

    /**
     * Освободить места.
     */
    public boolean releaseSeats(Long tripId, int numberOfSeats) {
        try {
            log.info("Releasing {} seats for trip {}", numberOfSeats, tripId);
            restTemplate.postForObject(
                    TRIP_SERVICE_URL + "/" + tripId + "/release?seats=" + numberOfSeats,
                    null,
                    Void.class
            );
            return true;
        } catch (Exception e) {
            log.error("Error releasing seats for trip {}: {}", tripId, e.getMessage());
            return false;
        }
    }

    /**
     * DTO для ответа от Trip Service.
     */
    public static class TripResponse {
        private Long id;
        private String title;
        private BigDecimal price;
        private String currency;
        private Integer availableSeats;
        private Integer totalSeats;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
        
        public Integer getTotalSeats() { return totalSeats; }
        public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    }
}

