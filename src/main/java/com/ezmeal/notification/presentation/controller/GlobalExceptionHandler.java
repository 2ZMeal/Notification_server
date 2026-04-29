package com.ezmeal.notification.presentation.controller;

import com.ezmeal.notification.domain.exception.NotificationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<Map<String, Object>> handleNotificationException(NotificationException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(Map.of(
                        "code", e.getCode(),
                        "message", e.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
