package com.travelmaster.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentResponse {

    private Long id;
    
    private String type;
    
    private Integer order;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private String provider;
    
    private String description;
    
    // Flight specific
    private String flightNumber;
    private String airline;
    private String departureAirport;
    private String arrivalAirport;
    private String cabinClass;
    
    // Hotel specific
    private String hotelName;
    private String hotelAddress;
    private String roomType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkIn;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkOut;
    private Integer starRating;
    
    // Transfer specific
    private String pickupLocation;
    private String dropoffLocation;
    private String vehicleType;
}

