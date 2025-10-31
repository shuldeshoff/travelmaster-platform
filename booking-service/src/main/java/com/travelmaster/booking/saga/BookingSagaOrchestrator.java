package com.travelmaster.booking.saga;

import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import com.travelmaster.booking.repository.BookingRepository;
import com.travelmaster.booking.service.TripServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Orchestrator для Saga Pattern в процессе бронирования.
 * 
 * Saga Steps:
 * 1. Создание бронирования (локально) ✓
 * 2. Резервирование мест в Trip Service → компенсация: освобождение мест
 * 3. Подтверждение бронирования (локально) ✓
 * 4. Инициация платежа в Payment Service → компенсация: отмена платежа
 * 5. Подтверждение оплаты (локально) ✓
 * 
 * При ошибке на любом шаге выполняются компенсирующие транзакции.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final TripServiceClient tripServiceClient;

    /**
     * Выполнение Saga для создания бронирования.
     * 
     * @param booking созданное бронирование (в статусе PENDING)
     * @return результат выполнения Saga
     */
    public SagaResult executeBookingCreationSaga(Booking booking) {
        log.info("Starting booking creation saga for booking: {}", booking.getId());

        try {
            // Step 1: Резервирование мест в Trip Service
            log.info("Step 1: Reserving seats in Trip Service");
            boolean seatsReserved = tripServiceClient.reserveSeats(
                    booking.getTripId(), 
                    booking.getNumberOfPassengers()
            );

            if (!seatsReserved) {
                log.error("Failed to reserve seats for booking: {}", booking.getId());
                return SagaResult.failure("Не удалось зарезервировать места");
            }

            // Step 2: Обновление статуса на CONFIRMED
            log.info("Step 2: Confirming booking");
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            log.info("Booking creation saga completed successfully for booking: {}", booking.getId());
            return SagaResult.success();

        } catch (Exception e) {
            log.error("Error in booking creation saga for booking: {}", booking.getId(), e);
            compensateBookingCreation(booking);
            return SagaResult.failure("Ошибка при создании бронирования: " + e.getMessage());
        }
    }

    /**
     * Выполнение Saga для оплаты бронирования.
     * 
     * @param booking бронирование для оплаты
     * @param paymentId ID платежа
     * @return результат выполнения Saga
     */
    public SagaResult executePaymentSaga(Booking booking, Long paymentId) {
        log.info("Starting payment saga for booking: {}", booking.getId());

        try {
            // В реальной системе здесь были бы вызовы к Payment Service
            // Пока просто обновляем статус
            
            log.info("Payment saga completed successfully for booking: {}", booking.getId());
            return SagaResult.success();

        } catch (Exception e) {
            log.error("Error in payment saga for booking: {}", booking.getId(), e);
            compensatePayment(booking, paymentId);
            return SagaResult.failure("Ошибка при оплате: " + e.getMessage());
        }
    }

    /**
     * Выполнение Saga для отмены бронирования.
     * 
     * @param booking бронирование для отмены
     * @return результат выполнения Saga
     */
    public SagaResult executeCancellationSaga(Booking booking) {
        log.info("Starting cancellation saga for booking: {}", booking.getId());

        try {
            // Step 1: Освобождение мест в Trip Service
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PAID) {
                log.info("Step 1: Releasing seats in Trip Service");
                tripServiceClient.releaseSeats(
                        booking.getTripId(), 
                        booking.getNumberOfPassengers()
                );
            }

            // Step 2: Возврат средств (если была оплата)
            if (booking.isPaid()) {
                log.info("Step 2: Initiating refund in Payment Service");
                // В реальной системе здесь был бы вызов к Payment Service для возврата
                booking.setRefundAmount(booking.getPaidAmount());
            }

            log.info("Cancellation saga completed successfully for booking: {}", booking.getId());
            return SagaResult.success();

        } catch (Exception e) {
            log.error("Error in cancellation saga for booking: {}", booking.getId(), e);
            return SagaResult.failure("Ошибка при отмене бронирования: " + e.getMessage());
        }
    }

    /**
     * Компенсация при ошибке создания бронирования.
     */
    private void compensateBookingCreation(Booking booking) {
        log.info("Compensating booking creation for booking: {}", booking.getId());

        try {
            // Если места были зарезервированы, освобождаем их
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                tripServiceClient.releaseSeats(booking.getTripId(), booking.getNumberOfPassengers());
            }

            // Откатываем статус к PENDING или помечаем как CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Автоматическая отмена из-за ошибки в процессе бронирования");
            bookingRepository.save(booking);

            log.info("Booking creation compensated for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("Error compensating booking creation for booking: {}", booking.getId(), e);
            // В production здесь должна быть запись в Dead Letter Queue или алерты
        }
    }

    /**
     * Компенсация при ошибке оплаты.
     */
    private void compensatePayment(Booking booking, Long paymentId) {
        log.info("Compensating payment for booking: {}, payment: {}", booking.getId(), paymentId);

        try {
            // В реальной системе здесь был бы вызов к Payment Service для отмены платежа
            
            log.info("Payment compensated for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("Error compensating payment for booking: {}", booking.getId(), e);
            // В production здесь должна быть запись в Dead Letter Queue или алерты
        }
    }

    /**
     * Результат выполнения Saga.
     */
    public static class SagaResult {
        private final boolean success;
        private final String errorMessage;

        private SagaResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static SagaResult success() {
            return new SagaResult(true, null);
        }

        public static SagaResult failure(String errorMessage) {
            return new SagaResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

