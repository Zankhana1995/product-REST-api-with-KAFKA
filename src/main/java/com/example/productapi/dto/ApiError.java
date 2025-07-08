package com.example.productapi.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApiError {
    private String path;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;

    public ApiError(String path, String message, int status, LocalDateTime timestamp) {
        this.path = path;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ApiError(String path, String message, int status, LocalDateTime timestamp,
                    Map<String, String> validationErrors) {
        this(path, message, status, timestamp);
        this.validationErrors = validationErrors;
    }
}