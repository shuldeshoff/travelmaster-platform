package com.travelmaster.payment.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentGatewayRequest {
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String ipAddress;
    private String userAgent;
}

