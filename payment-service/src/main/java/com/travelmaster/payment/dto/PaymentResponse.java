package com.travelmaster.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PaymentResponse {

    private Long id;
    
    private String paymentReference;
    
    private Long bookingId;
    
    private Long userId;
    
    private BigDecimal amount;
    
    private String currency;
    
    private String status;
    
    private String paymentMethod;
    
    private String gatewayTransactionId;
    
    private String gatewayName;
    
    private String cardLastFour;
    
    private String cardBrand;
    
    private BigDecimal refundAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refundedAt;
    
    private String refundReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime failedAt;
    
    private String failureReason;
    
    private Integer retryCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

