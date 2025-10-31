package com.travelmaster.booking.statemachine;

import com.travelmaster.booking.entity.BookingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Конфигурация State Machine для управления статусами бронирования.
 * 
 * Возможные переходы:
 * - PENDING → CONFIRMED (после резервирования в Trip Service)
 * - CONFIRMED → PAID (после успешной оплаты)
 * - PAID → COMPLETED (после завершения поездки)
 * - * → CANCELLED (отмена возможна на любом этапе кроме COMPLETED)
 */
@Slf4j
@Configuration
@EnableStateMachineFactory(name = "bookingStateMachineFactory")
public class BookingStateMachineConfig extends StateMachineConfigurerAdapter<BookingStatus, BookingEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<BookingStatus, BookingEvent> states) throws Exception {
        states
                .withStates()
                .initial(BookingStatus.PENDING)
                .states(EnumSet.allOf(BookingStatus.class))
                .end(BookingStatus.COMPLETED)
                .end(BookingStatus.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BookingStatus, BookingEvent> transitions) throws Exception {
        transitions
                // PENDING → CONFIRMED
                .withExternal()
                .source(BookingStatus.PENDING)
                .target(BookingStatus.CONFIRMED)
                .event(BookingEvent.CONFIRM)
                .and()
                
                // CONFIRMED → PAID
                .withExternal()
                .source(BookingStatus.CONFIRMED)
                .target(BookingStatus.PAID)
                .event(BookingEvent.PAY)
                .and()
                
                // PAID → COMPLETED
                .withExternal()
                .source(BookingStatus.PAID)
                .target(BookingStatus.COMPLETED)
                .event(BookingEvent.COMPLETE)
                .and()
                
                // PENDING → CANCELLED
                .withExternal()
                .source(BookingStatus.PENDING)
                .target(BookingStatus.CANCELLED)
                .event(BookingEvent.CANCEL)
                .and()
                
                // CONFIRMED → CANCELLED
                .withExternal()
                .source(BookingStatus.CONFIRMED)
                .target(BookingStatus.CANCELLED)
                .event(BookingEvent.CANCEL)
                .and()
                
                // PAID → CANCELLED (с возвратом средств)
                .withExternal()
                .source(BookingStatus.PAID)
                .target(BookingStatus.CANCELLED)
                .event(BookingEvent.CANCEL);
    }
}

