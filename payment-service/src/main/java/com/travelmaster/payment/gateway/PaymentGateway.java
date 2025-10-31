package com.travelmaster.payment.gateway;

import java.math.BigDecimal;

/**
 * Интерфейс для интеграции с платежными шлюзами.
 */
public interface PaymentGateway {

    /**
     * Обработать платеж.
     * 
     * @param request запрос на оплату
     * @return результат обработки
     */
    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);

    /**
     * Вернуть платеж.
     * 
     * @param transactionId ID транзакции
     * @param amount сумма возврата
     * @return результат возврата
     */
    PaymentGatewayResponse refundPayment(String transactionId, BigDecimal amount);

    /**
     * Проверить статус платежа.
     * 
     * @param transactionId ID транзакции
     * @return результат проверки
     */
    PaymentGatewayResponse checkPaymentStatus(String transactionId);

    /**
     * Название шлюза.
     */
    String getGatewayName();
}

