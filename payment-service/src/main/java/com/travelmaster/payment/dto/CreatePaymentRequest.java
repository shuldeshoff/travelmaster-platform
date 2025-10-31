package com.travelmaster.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Booking ID обязателен")
    private Long bookingId;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Метод оплаты обязателен")
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, SBP, etc.

    // Card details (должны быть зашифрованы в production)
    private String cardNumber;
    
    private String cardHolderName;
    
    private String expiryMonth;
    
    private String expiryYear;
    
    private String cvv;
}

