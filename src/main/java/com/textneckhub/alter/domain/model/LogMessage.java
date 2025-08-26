package com.textneckhub.alter.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {

    private String id;
    private String message;
    private LocalDateTime timestamp;
    private LogLevel level;
    private ServiceName service;

    public LogMessage(String message, String level, String service) {
        this.message = message;
        this.level = LogLevel.from(level);
        this.service = ServiceName.from(service);
        this.timestamp = LocalDateTime.now();
    }

    public String getLevel() {
        return level.name();
    }

    public String getService() {
        return service.name();
    }

}

enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, UNKNOWN;

    public static LogLevel from(String s) {
        if (s == null)
            return UNKNOWN;
        try {
            return LogLevel.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}

enum ServiceName {
    AUTH_SERVICE,
    BOARD_SERVICE,
    CORE_SERVICE,
    STA_SERVICE,
    UNKNOWN;

    public static ServiceName from(String s) {
        if (s == null)
            return UNKNOWN;
        try {
            return ServiceName.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}