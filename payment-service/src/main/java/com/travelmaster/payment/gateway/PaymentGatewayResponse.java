package com.travelmaster.payment.gateway;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private boolean success;
    private String transactionId;
    private String errorMessage;
    private String errorCode;
    private String cardLastFour;
    private String cardBrand;
}

