package com.ezmeal.notification.domain.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {

    private final String code;
    private final String message;
    private final org.springframework.http.HttpStatus status;

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
    }
}
