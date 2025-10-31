package com.travelmaster.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private Long tripId;
    private String cancellationReason;
    private BigDecimal refundAmount;
    private LocalDateTime cancelledAt;
}

