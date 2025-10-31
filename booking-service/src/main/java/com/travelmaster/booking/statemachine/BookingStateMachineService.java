package com.travelmaster.booking.statemachine;

import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для работы со State Machine бронирований.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStateMachineService {

    private final StateMachineFactory<BookingStatus, BookingEvent> stateMachineFactory;
    
    public static final String BOOKING_ID_HEADER = "bookingId";

    /**
     * Отправляет событие в State Machine для изменения статуса бронирования.
     */
    public boolean sendEvent(Long bookingId, BookingStatus currentStatus, BookingEvent event) {
        log.info("Sending event {} for booking {} with current status {}", event, bookingId, currentStatus);
        
        StateMachine<BookingStatus, BookingEvent> stateMachine = build(bookingId, currentStatus);
        
        Message<BookingEvent> message = MessageBuilder.withPayload(event)
                .setHeader(BOOKING_ID_HEADER, bookingId)
                .build();
        
        boolean result = stateMachine.sendEvent(message);
        
        log.info("Event {} processed for booking {}. Result: {}. New state: {}", 
                event, bookingId, result, stateMachine.getState().getId());
        
        return result;
    }

    /**
     * Получает новый статус после применения события.
     */
    public Optional<BookingStatus> getNewStatus(BookingStatus currentStatus, BookingEvent event) {
        StateMachine<BookingStatus, BookingEvent> stateMachine = build(0L, currentStatus);
        
        Message<BookingEvent> message = MessageBuilder.withPayload(event)
                .build();
        
        boolean success = stateMachine.sendEvent(message);
        
        if (success) {
            return Optional.ofNullable(stateMachine.getState())
                    .map(State::getId);
        }
        
        return Optional.empty();
    }

    /**
     * Проверяет, возможен ли переход из текущего статуса по событию.
     */
    public boolean isTransitionValid(BookingStatus currentStatus, BookingEvent event) {
        return getNewStatus(currentStatus, event).isPresent();
    }

    private StateMachine<BookingStatus, BookingEvent> build(Long bookingId, BookingStatus currentStatus) {
        StateMachine<BookingStatus, BookingEvent> stateMachine = stateMachineFactory.getStateMachine(
                "booking-" + bookingId
        );
        
        stateMachine.stop();
        
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<BookingStatus, BookingEvent>() {
                        @Override
                        public void preStateChange(State<BookingStatus, BookingEvent> state,
                                                  Message<BookingEvent> message,
                                                  Transition<BookingStatus, BookingEvent> transition,
                                                  StateMachine<BookingStatus, BookingEvent> stateMachine,
                                                  StateMachine<BookingStatus, BookingEvent> rootStateMachine) {
                            log.debug("State changing from {} to {}", 
                                    Optional.ofNullable(stateMachine.getState())
                                            .map(State::getId)
                                            .orElse(null),
                                    state.getId());
                        }
                    });
                    
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>(currentStatus, null, null, null)
                    );
                });
        
        stateMachine.start();
        
        return stateMachine;
    }
}

