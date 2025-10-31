package com.travelmaster.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service для работы с Refresh Tokens.
 * 
 * Refresh tokens используются для получения новых access tokens
 * без повторной аутентификации пользователя.
 */
@Slf4j
@Service
public class JwtRefreshTokenService {

    private final SecretKey refreshSecretKey;
    private final long refreshTokenValidityInMilliseconds;

    public JwtRefreshTokenService(
            @Value("${jwt.refresh-secret}") String refreshSecret,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidityInMilliseconds) {
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    /**
     * Генерирует refresh token для пользователя.
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(validity)
                .signWith(refreshSecretKey)
                .compact();
    }

    /**
     * Валидирует refresh token.
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check if it's actually a refresh token
            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                log.warn("Token is not a refresh token");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Извлекает email из refresh token.
     */
    public String getEmailFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(refreshSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}

