package com.travelmaster.trip.mapper;

import com.travelmaster.trip.dto.SegmentResponse;
import com.travelmaster.trip.dto.TripResponse;
import com.travelmaster.trip.entity.Segment;
import com.travelmaster.trip.entity.Trip;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TripMapper {

    public TripResponse toResponse(Trip trip) {
        if (trip == null) {
            return null;
        }

        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .departureDate(trip.getDepartureDate())
                .returnDate(trip.getReturnDate())
                .price(trip.getPrice())
                .currency(trip.getCurrency())
                .totalSeats(trip.getTotalSeats())
                .availableSeats(trip.getAvailableSeats())
                .status(trip.getStatus() != null ? trip.getStatus().name() : null)
                .provider(trip.getProvider())
                .segments(toSegmentResponses(trip.getSegments()))
                .inclusions(trip.getInclusions())
                .exclusions(trip.getExclusions())
                .minAge(trip.getMinAge())
                .maxAge(trip.getMaxAge())
                .createdAt(trip.getCreatedAt())
                .build();
    }

    private List<SegmentResponse> toSegmentResponses(List<Segment> segments) {
        if (segments == null) {
            return null;
        }
        
        return segments.stream()
                .map(this::toSegmentResponse)
                .collect(Collectors.toList());
    }

    private SegmentResponse toSegmentResponse(Segment segment) {
        if (segment == null) {
            return null;
        }

        return SegmentResponse.builder()
                .id(segment.getId())
                .type(segment.getType() != null ? segment.getType().name() : null)
                .order(segment.getOrder())
                .startTime(segment.getStartTime())
                .endTime(segment.getEndTime())
                .provider(segment.getProvider())
                .description(segment.getDescription())
                // Flight
                .flightNumber(segment.getFlightNumber())
                .airline(segment.getAirline())
                .departureAirport(segment.getDepartureAirport())
                .arrivalAirport(segment.getArrivalAirport())
                .cabinClass(segment.getCabinClass())
                // Hotel
                .hotelName(segment.getHotelName())
                .hotelAddress(segment.getHotelAddress())
                .roomType(segment.getRoomType())
                .checkIn(segment.getCheckIn())
                .checkOut(segment.getCheckOut())
                .starRating(segment.getStarRating())
                // Transfer
                .pickupLocation(segment.getPickupLocation())
                .dropoffLocation(segment.getDropoffLocation())
                .vehicleType(segment.getVehicleType())
                .build();
    }
}

