package com.travelmaster.booking.controller;

import com.travelmaster.booking.dto.BookingResponse;
import com.travelmaster.booking.dto.CreateBookingRequest;
import com.travelmaster.booking.entity.BookingStatus;
import com.travelmaster.booking.service.BookingService;
import com.travelmaster.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "API для управления бронированиями")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Создать бронирование",
            description = "Создает новое бронирование для аутентифицированного пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить бронирование по ID",
            description = "Возвращает информацию о бронировании",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id
    ) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    @Operation(
            summary = "Получить бронирование по номеру",
            description = "Возвращает информацию о бронировании по уникальному номеру",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> getBookingByReference(
            @PathVariable String reference
    ) {
        BookingResponse response = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Получить мои бронирования",
            description = "Возвращает все бронирования текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PageResponse<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<BookingResponse> response = bookingService.getUserBookings(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/status/{status}")
    @Operation(
            summary = "Получить мои бронирования по статусу",
            description = "Возвращает бронирования текущего пользователя с заданным статусом",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PageResponse<BookingResponse>> getMyBookingsByStatus(
            @PathVariable BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<BookingResponse> response = bookingService.getUserBookingsByStatus(userId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @Operation(
            summary = "Подтвердить бронирование",
            description = "Подтверждает бронирование и резервирует места",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id
    ) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Отменить бронирование",
            description = "Отменяет бронирование и освобождает места",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        BookingResponse response = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Завершить бронирование",
            description = "Отмечает бронирование как завершенное (после окончания поездки)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id
    ) {
        BookingResponse response = bookingService.completeBooking(id);
        return ResponseEntity.ok(response);
    }
}

