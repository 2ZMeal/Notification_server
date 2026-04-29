package com.ezmeal.notification.application.service;

import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.exception.NotificationErrorCode;
import com.ezmeal.notification.domain.exception.NotificationException;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.client.UserClient;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import com.ezmeal.notification.infrastructure.security.UserRoleCheck;
import lombok.RequiredArgsConstructor;
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
    public List<NotificationResponse> getNotifications(String userIdHeader, String roleHeader) {
        UserRoleCheck.requireAuthenticated(userIdHeader);
        UUID userId = UserRoleCheck.parseUserId(userIdHeader);
        return notificationRepository.findAllByUserIdAndDeletedAtIsNull(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    // GET /api/v1/notifications/{notificationId}
    public NotificationResponse getNotification(UUID notificationId, String userIdHeader) {
        UserRoleCheck.requireAuthenticated(userIdHeader);
        UUID userId = UserRoleCheck.parseUserId(userIdHeader);
        Notification notification = findByIdOrThrow(notificationId);
        UserRoleCheck.requireOwner(userId, notification.getUserId());
        notification.markAsRead();   // 단건 조회 시 자동 읽음
        return NotificationResponse.from(notification);
    }

    // DELETE /api/v1/notifications/{notificationId}
    public void deleteNotification(UUID notificationId, String userIdHeader) {
        UserRoleCheck.requireAuthenticated(userIdHeader);
        UUID userId = UserRoleCheck.parseUserId(userIdHeader);
        Notification notification = findByIdOrThrow(notificationId);
        UserRoleCheck.requireOwner(userId, notification.getUserId());
        notification.softDelete(userId.toString());
    }

    // POST /api/v1/admin/notifications
    public int sendAdminNotification(AdminNotificationRequest request, String roleHeader) {
        UserRoleCheck.requireMaster(roleHeader);

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

    private Notification findByIdOrThrow(UUID id) {
        return notificationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }
}
