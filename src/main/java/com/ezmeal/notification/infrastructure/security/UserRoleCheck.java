package com.ezmeal.notification.infrastructure.security;

import com.ezmeal.notification.domain.exception.NotificationErrorCode;
import com.ezmeal.notification.domain.exception.NotificationException;

import java.util.UUID;

/**
 * API Gateway 가 전달하는 헤더를 기반으로 권한을 검증한다.
 * X-User-Id    : 요청자 UUID
 * X-User-Role  : USER | VENDOR | MASTER
 *
 * 추후 common module 의 SecurityContextHolder 방식으로 리팩토링 예정
 */
public class UserRoleCheck {

    private UserRoleCheck() {}

    /**
     * 헤더가 없으면 NOTIFICATION_401 을 던진다.
     */
    public static void requireAuthenticated(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new NotificationException(NotificationErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * MASTER 권한이 아니면 NOTIFICATION_403 을 던진다.
     */
    public static void requireMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new NotificationException(NotificationErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 요청자가 알림의 소유자가 아니면 NOTIFICATION_403 을 던진다.
     */
    public static void requireOwner(UUID requestUserId, UUID notificationUserId) {
        if (!notificationUserId.equals(requestUserId)) {
            throw new NotificationException(NotificationErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 헤더 문자열을 UUID 로 파싱한다.
     */
    public static UUID parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new NotificationException(NotificationErrorCode.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new NotificationException(NotificationErrorCode.UNAUTHORIZED);
        }
    }
}
