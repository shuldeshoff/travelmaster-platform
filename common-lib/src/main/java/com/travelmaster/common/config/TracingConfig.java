package com.travelmaster.common.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация distributed tracing через Micrometer Observation API.
 */
@Configuration
public class TracingConfig {

    /**
     * Включает поддержку @Observed аннотаций для автоматического трейсинга.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}

