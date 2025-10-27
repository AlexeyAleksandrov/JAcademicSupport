package io.github.alexeyaleksandrov.jacademicsupport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for error responses.
 * Provides structured error information to API clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error message for the client
     */
    private String message;
    
    /**
     * Detailed error description (optional)
     */
    private String details;
    
    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Constructor with status and message
     */
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with status, message and details
     */
    public ErrorResponse(int status, String message, String details) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
