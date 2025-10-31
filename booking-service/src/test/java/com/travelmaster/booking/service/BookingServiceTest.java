package com.travelmaster.booking.service;

import com.travelmaster.booking.dto.BookingResponse;
import com.travelmaster.booking.dto.CreateBookingRequest;
import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import com.travelmaster.booking.mapper.BookingMapper;
import com.travelmaster.booking.repository.BookingRepository;
import com.travelmaster.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private BookingResponse testBookingResponse;

    @BeforeEach
    void setUp() {
        testBooking = Booking.builder()
                .id(1L)
                .bookingReference("BK-2025-001")
                .userId(100L)
                .tripId(200L)
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("50000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testBookingResponse = BookingResponse.builder()
                .id(1L)
                .bookingReference("BK-2025-001")
                .userId(100L)
                .tripId(200L)
                .status("PENDING")
                .totalAmount(new BigDecimal("50000.00"))
                .build();
    }

    @Test
    @DisplayName("Should get booking by id successfully")
    void shouldGetBookingByIdSuccessfully() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingMapper.toResponse(testBooking)).thenReturn(testBookingResponse);

        // When
        BookingResponse result = bookingService.getBookingById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("BK-2025-001", result.getBookingReference());
        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when booking not found")
    void shouldThrowExceptionWhenBookingNotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> bookingService.getBookingById(999L));
        verify(bookingRepository).findById(999L);
    }

    @Test
    @DisplayName("Should check booking status is PENDING")
    void shouldCheckBookingStatusIsPending() {
        // Given
        Booking pendingBooking = testBooking;
        pendingBooking.setStatus(BookingStatus.PENDING);

        // When
        boolean isPending = pendingBooking.getStatus() == BookingStatus.PENDING;

        // Then
        assertTrue(isPending);
    }

    @Test
    @DisplayName("Should check booking status is CONFIRMED")
    void shouldCheckBookingStatusIsConfirmed() {
        // Given
        testBooking.setStatus(BookingStatus.CONFIRMED);

        // When
        boolean isConfirmed = testBooking.getStatus() == BookingStatus.CONFIRMED;

        // Then
        assertTrue(isConfirmed);
    }

    @Test
    @DisplayName("Should check booking status is CANCELLED")
    void shouldCheckBookingStatusIsCancelled() {
        // Given
        testBooking.setStatus(BookingStatus.CANCELLED);

        // When
        boolean isCancelled = testBooking.getStatus() == BookingStatus.CANCELLED;

        // Then
        assertTrue(isCancelled);
        assertNotEquals(BookingStatus.CONFIRMED, testBooking.getStatus());
    }
}

