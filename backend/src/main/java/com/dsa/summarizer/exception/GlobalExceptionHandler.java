package com.dsa.summarizer.exception;

import com.dsa.summarizer.model.SummarizeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SummarizeResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        logger.error("Validation error: {}", errors);
        
        return ResponseEntity.badRequest()
                .body(SummarizeResponse.error("Validation failed: " + errors));
    }
    
    /**
     * Handles illegal arguments
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SummarizeResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.error("Invalid argument: {}", ex.getMessage());
        
        return ResponseEntity.badRequest()
                .body(SummarizeResponse.error(ex.getMessage()));
    }
    
    /**
     * Handles runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SummarizeResponse> handleRuntimeException(
            RuntimeException ex) {
        
        logger.error("Runtime error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SummarizeResponse.error("Internal error: " + ex.getMessage()));
    }
    
    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SummarizeResponse> handleGenericException(
            Exception ex) {
        
        logger.error("Unexpected error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SummarizeResponse.error("An unexpected error occurred. Please try again."));
    }
}