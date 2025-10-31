package com.travelmaster.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private Long id;
    
    private String title;
    
    private String description;
    
    private String origin;
    
    private String destination;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnDate;
    
    private BigDecimal price;
    
    private String currency;
    
    private Integer totalSeats;
    
    private Integer availableSeats;
    
    private String status;
    
    private String provider;
    
    private List<SegmentResponse> segments;
    
    private String inclusions;
    
    private String exclusions;
    
    private Integer minAge;
    
    private Integer maxAge;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

