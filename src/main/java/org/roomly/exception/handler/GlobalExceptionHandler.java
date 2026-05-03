package org.roomly.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions (Exception ex) {
        log.error("An error occurred: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("An error occurred: " + ex.getMessage());
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityExceptions (SecurityException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return ResponseEntity.status(401).body("Unauthorized access: " + ex.getMessage());
    }
}
