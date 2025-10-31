package com.travelmaster.payment.event;

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
public class PaymentRefundedEvent {
    private Long paymentId;
    private String paymentReference;
    private Long bookingId;
    private Long userId;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;
}

