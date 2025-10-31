package com.travelmaster.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для audit log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    private Long userId;
    private String userEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String ipAddress;
    private String userAgent;
    private String details;
    private String status;
    private String errorMessage;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String serviceName;

    /**
     * Converts DTO to entity for persistence.
     */
    public AuditLog toEntity() {
        return AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details(details)
                .status(status)
                .errorMessage(errorMessage)
                .timestamp(timestamp)
                .serviceName(serviceName)
                .build();
    }
}

