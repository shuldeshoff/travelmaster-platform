package com.travelmaster.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server для централизованного управления конфигурацией.
 * 
 * Особенности:
 * - Centralized configuration management
 * - Git-backed configuration
 * - Encrypted properties support
 * - Environment-specific profiles
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

