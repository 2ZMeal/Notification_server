package com.ezmeal.notification.application.service;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.common.exception.types.UnauthorizedException;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.exception.NotificationErrorCode;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.client.UserClient;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;
    private final UserClient userClient;

    // GET /api/v1/notifications
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        UUID userId = UUID.fromString(getCurrentPrincipal().getUserId());
        return notificationRepository.findAllByUserIdAndDeletedAtIsNull(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    // GET /api/v1/notifications/{notificationId}
    public NotificationResponse getNotification(UUID notificationId) {
        UUID userId = UUID.fromString(getCurrentPrincipal().getUserId());
        Notification notification = findByIdOrThrow(notificationId);
        requireOwner(userId, notification.getUserId());
        notification.markAsRead();   // 단건 조회 시 자동 읽음
        return NotificationResponse.from(notification);
    }

    // DELETE /api/v1/notifications/{notificationId}
    public void deleteNotification(UUID notificationId) {
        UUID userId = UUID.fromString(getCurrentPrincipal().getUserId());
        Notification notification = findByIdOrThrow(notificationId);
        requireOwner(userId, notification.getUserId());
        notification.softDelete(userId.toString());
    }

    // POST /api/v1/admin/notifications
    public int sendAdminNotification(AdminNotificationRequest request) {
        requireAdmin(getCurrentPrincipal().getRole());

        if (request.getUserId() != null) {
            // 단일 유저 발송
            Notification notification = Notification.create(
                    request.getUserId(), NotificationType.ADMIN_BROADCAST,
                    request.getMessage(), request.getChannel()
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
            return 1;
        }

        // TODO: 전체 유저 대상 브로드캐스트는 Kafka 이벤트 방식(admin.broadcast 토픽)으로 구현 예정
        //       OOM/타임아웃 위험으로 전체 userId 조회 방식 미구현
        // List<UUID> allIds = userClient.getAllUserIds();
        // for (UUID uid : allIds) { ... }
        return 0;
    }

    private CustomUserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new UnauthorizedException(NotificationErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private void requireAdmin(Role role) {
        if (role != Role.ADMIN) {
            throw new ForbiddenException(NotificationErrorCode.ACCESS_DENIED);
        }
    }

    private void requireOwner(UUID requesterId, UUID ownerId) {
        if (!ownerId.equals(requesterId)) {
            throw new ForbiddenException(NotificationErrorCode.ACCESS_DENIED);
        }
    }

    private Notification findByIdOrThrow(UUID id) {
        return notificationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }
}
