package com.travelmaster.common.util;

/**
 * Утилиты для работы со строками.
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Маскирует email адрес для логов.
     * Example: john.doe@example.com -> j***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 1) {
            return "*@" + domain;
        }
        return username.charAt(0) + "***@" + domain;
    }

    /**
     * Маскирует номер телефона для логов.
     * Example: +79991234567 -> +7***4567
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        int lastFourStart = phone.length() - 4;
        return phone.substring(0, 2) + "***" + phone.substring(lastFourStart);
    }

    /**
     * Маскирует номер карты для логов.
     * Example: 4242424242424242 -> **** **** **** 4242
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * Проверяет, что строка не null и не пустая.
     */
    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Генерирует случайный код указанной длины.
     */
    public static String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }
}

