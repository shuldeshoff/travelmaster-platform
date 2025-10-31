package com.travelmaster.booking.service;

import com.travelmaster.booking.dto.BookingResponse;
import com.travelmaster.booking.dto.CreateBookingRequest;
import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import com.travelmaster.booking.entity.Passenger;
import com.travelmaster.booking.event.*;
import com.travelmaster.booking.mapper.BookingMapper;
import com.travelmaster.booking.repository.BookingRepository;
import com.travelmaster.booking.statemachine.BookingEvent;
import com.travelmaster.booking.statemachine.BookingStateMachineService;
import com.travelmaster.common.dto.PageResponse;
import com.travelmaster.common.exception.BusinessException;
import com.travelmaster.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingStateMachineService stateMachineService;
    private final BookingEventPublisher eventPublisher;
    private final TripServiceClient tripServiceClient;

    /**
     * Создание нового бронирования.
     */
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        log.info("Creating booking for user {} and trip {}", userId, request.getTripId());

        // 1. Проверяем существование поездки
        var tripResponse = tripServiceClient.getTripById(request.getTripId())
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + request.getTripId()));

        // 2. Проверяем доступность мест
        int availableSeats = tripResponse.getAvailableSeats();
        int requestedSeats = request.getPassengers().size();
        
        if (requestedSeats > availableSeats) {
            throw new BusinessException("Недостаточно свободных мест. Доступно: " + availableSeats + ", запрошено: " + requestedSeats);
        }

        // 3. Рассчитываем стоимость
        BigDecimal totalAmount = tripResponse.getPrice().multiply(BigDecimal.valueOf(requestedSeats));

        // 4. Создаем бронирование
        Booking booking = bookingMapper.toEntity(request);
        booking.setUserId(userId);
        booking.setBookingReference(generateBookingReference());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        booking.setCurrency(tripResponse.getCurrency());
        booking.setNumberOfPassengers(requestedSeats);

        // 5. Добавляем пассажиров
        for (CreateBookingRequest.PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = bookingMapper.toPassengerEntity(passengerRequest);
            booking.addPassenger(passenger);
        }

        // 6. Сохраняем
        booking = bookingRepository.save(booking);

        log.info("Booking created with reference: {}", booking.getBookingReference());

        // 7. Публикуем событие
        eventPublisher.publishBookingCreated(BookingCreatedEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(userId)
                .tripId(booking.getTripId())
                .numberOfPassengers(booking.getNumberOfPassengers())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .createdAt(booking.getCreatedAt())
                .build());

        return bookingMapper.toResponse(booking);
    }

    /**
     * Подтверждение бронирования (резервирование мест).
     */
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        log.info("Confirming booking {}", bookingId);

        Booking booking = findBookingById(bookingId);

        // Проверяем возможность перехода
        if (!stateMachineService.isTransitionValid(booking.getStatus(), BookingEvent.CONFIRM)) {
            throw new BusinessException("Cannot confirm booking in status: " + booking.getStatus());
        }

        // Резервируем места в Trip Service
        boolean reserved = tripServiceClient.reserveSeats(booking.getTripId(), booking.getNumberOfPassengers());
        if (!reserved) {
            throw new BusinessException("Failed to reserve seats for trip: " + booking.getTripId());
        }

        // Изменяем статус
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} confirmed", bookingId);

        // Публикуем событие
        eventPublisher.publishBookingConfirmed(BookingConfirmedEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .confirmedAt(LocalDateTime.now())
                .build());

        return bookingMapper.toResponse(booking);
    }

    /**
     * Обработка успешной оплаты.
     */
    @Transactional
    public BookingResponse markAsPaid(Long bookingId, Long paymentId, BigDecimal amount) {
        log.info("Marking booking {} as paid. Payment: {}, Amount: {}", bookingId, paymentId, amount);

        Booking booking = findBookingById(bookingId);

        // Проверяем возможность перехода
        if (!stateMachineService.isTransitionValid(booking.getStatus(), BookingEvent.PAY)) {
            throw new BusinessException("Cannot pay for booking in status: " + booking.getStatus());
        }

        // Проверяем сумму
        if (amount.compareTo(booking.getTotalAmount()) < 0) {
            throw new BusinessException("Insufficient payment amount. Required: " + booking.getTotalAmount() + ", received: " + amount);
        }

        // Обновляем статус
        booking.markAsPaid(paymentId, amount);
        booking = bookingRepository.save(booking);

        log.info("Booking {} marked as paid", bookingId);

        // Публикуем событие
        eventPublisher.publishBookingPaid(BookingPaidEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .paymentId(paymentId)
                .paidAmount(amount)
                .currency(booking.getCurrency())
                .paidAt(booking.getPaidAt())
                .build());

        return bookingMapper.toResponse(booking);
    }

    /**
     * Отмена бронирования.
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason) {
        log.info("Cancelling booking {} with reason: {}", bookingId, reason);

        Booking booking = findBookingById(bookingId);

        // Проверяем возможность отмены
        if (!booking.canBeCancelled()) {
            throw new BusinessException("Cannot cancel booking in status: " + booking.getStatus());
        }

        // Отменяем бронирование
        booking.cancel(reason);

        // Если была оплата, рассчитываем возврат
        BigDecimal refundAmount = null;
        if (booking.isPaid()) {
            refundAmount = booking.getPaidAmount();
            booking.setRefundAmount(refundAmount);
            booking.setRefundedAt(LocalDateTime.now());
        }

        // Освобождаем места в Trip Service
        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PAID) {
            tripServiceClient.releaseSeats(booking.getTripId(), booking.getNumberOfPassengers());
        }

        booking = bookingRepository.save(booking);

        log.info("Booking {} cancelled", bookingId);

        // Публикуем событие
        eventPublisher.publishBookingCancelled(BookingCancelledEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .cancellationReason(reason)
                .refundAmount(refundAmount)
                .cancelledAt(booking.getCancelledAt())
                .build());

        return bookingMapper.toResponse(booking);
    }

    /**
     * Завершение бронирования (после окончания поездки).
     */
    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        log.info("Completing booking {}", bookingId);

        Booking booking = findBookingById(bookingId);

        if (!stateMachineService.isTransitionValid(booking.getStatus(), BookingEvent.COMPLETE)) {
            throw new BusinessException("Cannot complete booking in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} completed", bookingId);

        // Публикуем событие
        eventPublisher.publishBookingCompleted(BookingCompletedEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .completedAt(LocalDateTime.now())
                .build());

        return bookingMapper.toResponse(booking);
    }

    /**
     * Получение бронирования по ID.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = findBookingById(id);
        return bookingMapper.toResponse(booking);
    }

    /**
     * Получение бронирования по reference.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + reference));
        return bookingMapper.toResponse(booking);
    }

    /**
     * Получение всех бронирований пользователя.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return PageResponse.<BookingResponse>builder()
                .content(bookingMapper.toResponseList(bookings.getContent()))
                .page(bookings.getNumber())
                .size(bookings.getSize())
                .totalElements(bookings.getTotalElements())
                .totalPages(bookings.getTotalPages())
                .build();
    }

    /**
     * Получение бронирований пользователя по статусу.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookingsByStatus(Long userId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.<BookingResponse>builder()
                .content(bookingMapper.toResponseList(bookings.getContent()))
                .page(bookings.getNumber())
                .size(bookings.getSize())
                .totalElements(bookings.getTotalElements())
                .totalPages(bookings.getTotalPages())
                .build();
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + id));
    }

    private String generateBookingReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TM-" + timestamp + "-" + random;
    }
}

