package com.travelmaster.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

/**
 * Security Headers Configuration для Gateway Service.
 * 
 * Добавляет следующие security headers:
 * - Strict-Transport-Security (HSTS)
 * - X-Content-Type-Options
 * - X-Frame-Options
 * - X-XSS-Protection
 * - Content-Security-Policy
 * - Referrer-Policy
 * - Permissions-Policy
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public WebFilter securityHeadersFilter() {
        return (exchange, chain) -> {
            var response = exchange.getResponse();
            var headers = response.getHeaders();
            
            // HSTS: Force HTTPS for 1 year including subdomains
            headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            
            // Prevent MIME type sniffing
            headers.add("X-Content-Type-Options", "nosniff");
            
            // Clickjacking protection
            headers.add("X-Frame-Options", "DENY");
            
            // XSS protection (legacy, but still useful)
            headers.add("X-XSS-Protection", "1; mode=block");
            
            // Content Security Policy
            headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'");
            
            // Referrer policy
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Permissions Policy (formerly Feature-Policy)
            headers.add("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "accelerometer=()");
            
            // Remove server info
            headers.remove("Server");
            headers.remove("X-Powered-By");
            
            return chain.filter(exchange);
        };
    }
}

