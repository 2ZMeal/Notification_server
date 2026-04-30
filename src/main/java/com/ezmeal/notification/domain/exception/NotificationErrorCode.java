package com.ezmeal.notification.domain.exception;

import com.ezmeal.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND("NOTIFICATION_001", "알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("NOTIFICATION_002", "대상 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("NOTIFICATION_403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("NOTIFICATION_401", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
