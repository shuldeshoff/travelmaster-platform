package com.travelmaster.trip.controller;

import com.travelmaster.common.dto.PageResponse;
import com.travelmaster.trip.dto.TripResponse;
import com.travelmaster.trip.dto.TripSearchRequest;
import com.travelmaster.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "API управления поездками")
public class TripController {

    private final TripService tripService;

    @PostMapping("/search")
    @Operation(summary = "Поиск поездок")
    public ResponseEntity<PageResponse<TripResponse>> searchTrips(
            @Valid @RequestBody TripSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TripResponse> response = tripService.searchTrips(request, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Получить все доступные поездки")
    public ResponseEntity<PageResponse<TripResponse>> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TripResponse> response = tripService.getAllTrips(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить поездку по ID")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(summary = "Получить количество доступных поездок")
    public ResponseEntity<Long> countAvailableTrips() {
        long count = tripService.countAvailableTrips();
        return ResponseEntity.ok(count);
    }
}

