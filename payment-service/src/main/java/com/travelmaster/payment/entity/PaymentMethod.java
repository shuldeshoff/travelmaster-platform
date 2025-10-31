package com.travelmaster.payment.entity;

/**
 * Методы оплаты.
 */
public enum PaymentMethod {
    CREDIT_CARD,    // Банковская карта
    DEBIT_CARD,     // Дебетовая карта
    SBP,            // Система быстрых платежей
    BANK_TRANSFER,  // Банковский перевод
    WALLET          // Электронный кошелек (YooMoney, QIWI и т.д.)
}

