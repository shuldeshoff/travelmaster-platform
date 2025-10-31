package com.travelmaster.trip.service;

import com.travelmaster.common.dto.PageResponse;
import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.trip.dto.TripResponse;
import com.travelmaster.trip.dto.TripSearchRequest;
import com.travelmaster.trip.entity.Trip;
import com.travelmaster.trip.entity.TripStatus;
import com.travelmaster.trip.mapper.TripMapper;
import com.travelmaster.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;

    @Cacheable(value = "trips", key = "#id")
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        log.debug("Получение поездки с ID: {}", id);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip", id));
        return tripMapper.toResponse(trip);
    }

    @Transactional(readOnly = true)
    public PageResponse<TripResponse> searchTrips(TripSearchRequest request, int page, int size) {
        log.info("Поиск поездок: {} → {}, passengers: {}", 
                request.getOrigin(), request.getDestination(), request.getPassengers());

        // Создание Pageable с сортировкой
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(page, size, sort);

        // Конвертация LocalDate в LocalDateTime
        LocalDateTime startDate = request.getDepartureDate().atStartOfDay();
        LocalDateTime endDate = request.getDepartureDate().atTime(LocalTime.MAX);

        Page<Trip> tripsPage = tripRepository.searchTrips(
                request.getOrigin(),
                request.getDestination(),
                startDate,
                endDate,
                request.getPassengers(),
                pageable
        );

        // Фильтрация по цене если указано
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            BigDecimal min = request.getMinPrice() != null ? request.getMinPrice() : BigDecimal.ZERO;
            BigDecimal max = request.getMaxPrice() != null ? request.getMaxPrice() : BigDecimal.valueOf(Double.MAX_VALUE);
            
            tripsPage = tripsPage.map(trip -> {
                if (trip.getPrice().compareTo(min) >= 0 && trip.getPrice().compareTo(max) <= 0) {
                    return trip;
                }
                return null;
            }).map(trip -> trip);
        }

        return mapToPageResponse(tripsPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<TripResponse> getAllTrips(int page, int size) {
        log.debug("Получение всех поездок: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDate").ascending());
        Page<Trip> tripsPage = tripRepository.findByStatus(TripStatus.AVAILABLE, pageable);
        return mapToPageResponse(tripsPage);
    }

    @CacheEvict(value = "trips", key = "#tripId")
    @Transactional
    public void reserveSeats(Long tripId, int count) {
        log.info("Резервирование {} мест для поездки ID: {}", count, tripId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip", tripId));

        trip.reserveSeats(count);

        // Обновляем статус если мест не осталось
        if (trip.getAvailableSeats() == 0) {
            trip.setStatus(TripStatus.FULL);
        }

        tripRepository.save(trip);
        log.info("Места зарезервированы. Осталось доступных: {}", trip.getAvailableSeats());
    }

    @CacheEvict(value = "trips", key = "#tripId")
    @Transactional
    public void releaseSeats(Long tripId, int count) {
        log.info("Освобождение {} мест для поездки ID: {}", count, tripId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip", tripId));

        trip.releaseSeats(count);

        // Обновляем статус если были мест не было, а теперь появились
        if (trip.getStatus() == TripStatus.FULL && trip.getAvailableSeats() > 0) {
            trip.setStatus(TripStatus.AVAILABLE);
        }

        tripRepository.save(trip);
        log.info("Места освобождены. Доступно: {}", trip.getAvailableSeats());
    }

    @Transactional(readOnly = true)
    public long countAvailableTrips() {
        return tripRepository.countAvailableTrips();
    }

    private Sort createSort(String sortBy, String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        return switch (sortBy.toLowerCase()) {
            case "price" -> Sort.by(sortDirection, "price");
            case "departuredate" -> Sort.by(sortDirection, "departureDate");
            default -> Sort.by(Sort.Direction.ASC, "departureDate");
        };
    }

    private PageResponse<TripResponse> mapToPageResponse(Page<Trip> page) {
        return PageResponse.<TripResponse>builder()
                .content(page.getContent().stream()
                        .map(tripMapper::toResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}

