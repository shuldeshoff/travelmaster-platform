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
public class BookingPaidEvent {
    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private Long paymentId;
    private BigDecimal paidAmount;
    private String currency;
    private LocalDateTime paidAt;
}

