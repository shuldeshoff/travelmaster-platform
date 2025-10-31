package com.travelmaster.trip.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchRequest {

    @NotBlank(message = "Origin обязателен")
    private String origin;
    
    @NotBlank(message = "Destination обязателен")
    private String destination;
    
    @NotNull(message = "Дата отправления обязательна")
    private LocalDate departureDate;
    
    private LocalDate returnDate;
    
    @Min(value = 1, message = "Минимум 1 пассажир")
    private Integer passengers = 1;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;
    
    private String sortBy = "price"; // price, departureDate, duration
    
    private String sortDirection = "ASC"; // ASC, DESC
}

