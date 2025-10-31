package com.travelmaster.common.constant;

/**
 * Константы для ролей пользователей.
 */
public final class RoleConstants {

    public static final String ROLE_TRAVELER = "ROLE_TRAVELER";
    public static final String ROLE_AGENT = "ROLE_AGENT";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Для @PreAuthorize без префикса ROLE_
    public static final String TRAVELER = "TRAVELER";
    public static final String AGENT = "AGENT";
    public static final String ADMIN = "ADMIN";

    private RoleConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

