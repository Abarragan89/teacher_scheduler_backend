package com.mathfactmissions.teacherscheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    // ── Business Logic Exceptions ────────────────────────────────────────────
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        System.err.println("⚠️ Email already exists: " + ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Email Already Exists", ex.getMessage());
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        System.err.println("⚠️ User not found: " + ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }
    
    // ── General Runtime Exceptions (intentional business errors) ─────────────
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        System.err.println("⚠️ Runtime exception: " + ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }
    
    // ── Validation Exceptions ────────────────────────────────────────────────
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage()
            ));
        System.err.println("⚠️ Validation error: " + fieldErrors);
        
        Map<String, Object> errorBody = buildErrorBody(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request fields");
        errorBody.put("messages", fieldErrors);
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }
    
    // ── Catch-All (unexpected errors — never expose internals) ───────────────
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
        // Log full stack trace server-side only
        System.err.println("❌ Unexpected error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        ex.printStackTrace();
        
        // Never expose internal error details to the frontend
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }
    
    // ── Helper Methods ───────────────────────────────────────────────────────
    
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        return new ResponseEntity<>(buildErrorBody(status, error, message), status);
    }
    
    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", error);
        errorBody.put("message", message);
        return errorBody;
    }
}

