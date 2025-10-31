package com.travelmaster.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock реализация платежного шлюза для тестирования.
 * В production это будет реальная интеграция с YooKassa, Tinkoff или другими PSP.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    private final Random random = new Random();

    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via MockPaymentGateway: {}", request.getPaymentReference());

        // Симулируем задержку сети
        simulateNetworkDelay();

        // Получаем последние 4 цифры карты
        String cardLastFour = extractLastFour(request.getCardNumber());
        String cardBrand = detectCardBrand(request.getCardNumber());

        // Симулируем случайные ошибки (5% вероятность)
        if (random.nextInt(100) < 5) {
            log.error("Payment failed (simulated error)");
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .errorMessage("Insufficient funds")
                    .errorCode("INSUFFICIENT_FUNDS")
                    .cardLastFour(cardLastFour)
                    .cardBrand(cardBrand)
                    .build();
        }

        // Успешная обработка
        String transactionId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("Payment processed successfully. Transaction ID: {}", transactionId);
        
        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .cardLastFour(cardLastFour)
                .cardBrand(cardBrand)
                .build();
    }

    @Override
    public PaymentGatewayResponse refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing refund for transaction {} amount {}", transactionId, amount);

        simulateNetworkDelay();

        // Симулируем успешный возврат
        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId("REFUND-" + transactionId)
                .build();
    }

    @Override
    public PaymentGatewayResponse checkPaymentStatus(String transactionId) {
        log.info("Checking payment status for transaction: {}", transactionId);

        simulateNetworkDelay();

        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .build();
    }

    @Override
    public String getGatewayName() {
        return "MockGateway";
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(100 + random.nextInt(400)); // 100-500ms задержка
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }
        
        char firstDigit = cardNumber.charAt(0);
        switch (firstDigit) {
            case '4':
                return "VISA";
            case '5':
                return "MASTERCARD";
            case '2':
                return "MIR";
            default:
                return "OTHER";
        }
    }
}

