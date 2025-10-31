package com.travelmaster.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service для записи audit logs.
 * 
 * Используется для логирования всех важных операций в системе.
 */
@Slf4j
@Service
public class AuditLogService {

    /**
     * Записывает audit log для операции.
     */
    public void logAction(AuditLogEntry entry) {
        // В production это должно писать в отдельную таблицу БД
        // или external audit system (ELK, Splunk, etc.)
        log.info("AUDIT: action={}, user={}, entity={}/{}, status={}, ip={}", 
            entry.getAction(),
            entry.getUserEmail(),
            entry.getEntityType(),
            entry.getEntityId(),
            entry.getStatus(),
            entry.getIpAddress()
        );

        // TODO: Persist to database or external audit system
        // auditLogRepository.save(entry.toEntity());
    }

    /**
     * Записывает успешную операцию.
     */
    public void logSuccess(String action, String entityType, String entityId, Long userId, String userEmail) {
        logAction(AuditLogEntry.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .userEmail(userEmail)
                .status("SUCCESS")
                .build());
    }

    /**
     * Записывает failed операцию.
     */
    public void logFailure(String action, String entityType, String entityId, Long userId, String userEmail, String errorMessage) {
        logAction(AuditLogEntry.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .userEmail(userEmail)
                .status("FAILURE")
                .errorMessage(errorMessage)
                .build());
    }
}

