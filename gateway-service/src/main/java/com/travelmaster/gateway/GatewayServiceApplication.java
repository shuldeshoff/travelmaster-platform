package com.travelmaster.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r
                        .path("/api/v1/auth/**", "/api/v1/users/**")
                        .uri("http://localhost:8081"))
                
                // Trip Service routes (для будущего)
                .route("trip-service", r -> r
                        .path("/api/v1/trips/**")
                        .uri("http://localhost:8082"))
                
                // Booking Service routes (для будущего)
                .route("booking-service", r -> r
                        .path("/api/v1/bookings/**")
                        .uri("http://localhost:8083"))
                
                // Payment Service routes (для будущего)
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**")
                        .uri("http://localhost:8084"))
                
                // Analytics Service routes (для будущего)
                .route("analytics-service", r -> r
                        .path("/api/v1/analytics/**")
                        .uri("http://localhost:8086"))
                
                .build();
    }
}

