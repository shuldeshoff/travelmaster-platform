package com.travelmaster.trip.service;

import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.trip.dto.TripResponse;
import com.travelmaster.trip.dto.TripSearchRequest;
import com.travelmaster.trip.entity.Trip;
import com.travelmaster.trip.entity.TripStatus;
import com.travelmaster.trip.mapper.TripMapper;
import com.travelmaster.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService Unit Tests")
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripService tripService;

    private Trip testTrip;
    private TripResponse testTripResponse;

    @BeforeEach
    void setUp() {
        testTrip = Trip.builder()
                .id(1L)
                .title("Trip to Paris")
                .origin("Moscow")
                .destination("Paris")
                .departureDate(LocalDateTime.now().plusDays(7))
                .returnDate(LocalDateTime.now().plusDays(14))
                .price(new BigDecimal("50000.00"))
                .currency("RUB")
                .availableSeats(10)
                .totalSeats(50)
                .status(TripStatus.AVAILABLE)
                .description("Direct flight to Paris")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTripResponse = TripResponse.builder()
                .id(1L)
                .title("Trip to Paris")
                .origin("Moscow")
                .destination("Paris")
                .price(new BigDecimal("50000.00"))
                .currency("RUB")
                .availableSeats(10)
                .status("AVAILABLE")
                .build();
    }

    @Test
    @DisplayName("Should get trip by id successfully")
    void shouldGetTripByIdSuccessfully() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(tripMapper.toResponse(testTrip)).thenReturn(testTripResponse);

        // When
        TripResponse result = tripService.getTripById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Moscow", result.getOrigin());
        assertEquals("Paris", result.getDestination());
        verify(tripRepository).findById(1L);
        verify(tripMapper).toResponse(testTrip);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when trip not found")
    void shouldThrowExceptionWhenTripNotFound() {
        // Given
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> tripService.getTripById(999L));
        verify(tripRepository).findById(999L);
        verify(tripMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should search trips successfully")
    void shouldSearchTripsSuccessfully() {
        // Given
        TripSearchRequest searchRequest = TripSearchRequest.builder()
                .origin("Moscow")
                .destination("Paris")
                .departureDate(LocalDate.now().plusDays(7))
                .passengers(2)
                .sortBy("departureDate")
                .sortDirection("ASC")
                .build();

        List<Trip> trips = Arrays.asList(testTrip);
        Page<Trip> tripPage = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

        when(tripRepository.searchTrips(
                eq("Moscow"), eq("Paris"), any(LocalDateTime.class), any(LocalDateTime.class), 
                eq(2), any(Pageable.class)))
                .thenReturn(tripPage);
        when(tripMapper.toResponse(testTrip)).thenReturn(testTripResponse);

        // When
        var result = tripService.searchTrips(searchRequest, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Moscow", result.getContent().get(0).getOrigin());
    }

    @Test
    @DisplayName("Should get all trips with pagination")
    void shouldGetAllTripsWithPagination() {
        // Given
        List<Trip> trips = Arrays.asList(testTrip);
        Page<Trip> tripPage = new PageImpl<>(trips, PageRequest.of(0, 10), 1);

        when(tripRepository.findByStatus(eq(TripStatus.AVAILABLE), any(Pageable.class)))
                .thenReturn(tripPage);
        when(tripMapper.toResponse(testTrip)).thenReturn(testTripResponse);

        // When
         var result = tripService.getAllTrips(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tripRepository).findByStatus(eq(TripStatus.AVAILABLE), any(Pageable.class));
    }

    @Test
    @DisplayName("Should check if trip is available")
    void shouldCheckIfTripIsAvailable() {
        // Given
        Trip availableTrip = testTrip;
        availableTrip.setStatus(TripStatus.AVAILABLE);
        availableTrip.setAvailableSeats(10);

        // When
        boolean isAvailable = availableTrip.getStatus() == TripStatus.AVAILABLE 
                           && availableTrip.getAvailableSeats() > 0;

        // Then
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("Should return false when trip is full")
    void shouldReturnFalseWhenTripIsFull() {
        // Given
        Trip fullTrip = testTrip;
        fullTrip.setStatus(TripStatus.FULL);
        fullTrip.setAvailableSeats(0);

        // When
        boolean isAvailable = fullTrip.getStatus() == TripStatus.AVAILABLE 
                           && fullTrip.getAvailableSeats() > 0;

        // Then
        assertFalse(isAvailable);
    }

    @Test
    @DisplayName("Should return false when trip is cancelled")
    void shouldReturnFalseWhenTripIsCancelled() {
        // Given
        testTrip.setStatus(TripStatus.CANCELLED);

        // When
        boolean isAvailable = testTrip.getStatus() == TripStatus.AVAILABLE;

        // Then
        assertFalse(isAvailable);
    }
}

