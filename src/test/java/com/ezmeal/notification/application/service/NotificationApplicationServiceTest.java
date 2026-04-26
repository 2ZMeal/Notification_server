package com.ezmeal.notification.application.service;

import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.exception.NotificationException;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.client.UserClient;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import com.ezmeal.notification.infrastructure.security.UserRoleCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @InjectMocks
    NotificationApplicationService service;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    NotificationRouter notificationRouter;

    @Mock
    UserClient userClient;

    @Test
    @DisplayName("getNotifications - 본인 알림 목록을 반환한다")
    void getNotifications_success() {
        UUID userId = UUID.randomUUID();
        String userIdHeader = userId.toString();
        Notification notification = Notification.create(userId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(userId);
            given(notificationRepository.findAllByUserIdAndDeletedAtIsNull(userId))
                    .willReturn(List.of(notification));

            List<NotificationResponse> result = service.getNotifications(userIdHeader, "USER");

            assertThat(result).hasSize(1);
        }
    }

    @Test
    @DisplayName("getNotification - 단건 조회 시 isRead가 true로 변경된다")
    void getNotification_marksAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        String userIdHeader = userId.toString();
        Notification notification = Notification.create(userId, NotificationType.PAYMENT_SUCCESS,
                "결제 완료", NotificationChannel.EMAIL);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(userId);
            given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                    .willReturn(Optional.of(notification));

            NotificationResponse result = service.getNotification(notificationId, userIdHeader);

            assertThat(result.isRead()).isTrue();
        }
    }

    @Test
    @DisplayName("getNotification - 본인 알림이 아니면 NOTIFICATION_403 예외가 발생한다")
    void getNotification_accessDenied() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        String userIdHeader = requesterId.toString();
        Notification notification = Notification.create(ownerId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(requesterId);
            mock.when(() -> UserRoleCheck.requireOwner(requesterId, ownerId))
                    .thenThrow(new NotificationException(
                            com.ezmeal.notification.domain.exception.NotificationErrorCode.ACCESS_DENIED));
            given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                    .willReturn(Optional.of(notification));

            assertThatThrownBy(() -> service.getNotification(notificationId, userIdHeader))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("접근 권한이 없습니다");
        }
    }

    @Test
    @DisplayName("getNotification - 존재하지 않는 알림이면 NOTIFICATION_001 예외가 발생한다")
    void getNotification_notFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        String userIdHeader = userId.toString();

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(userId);
            given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getNotification(notificationId, userIdHeader))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("알림을 찾을 수 없습니다");
        }
    }

    @Test
    @DisplayName("deleteNotification - 정상적으로 소프트 삭제된다")
    void deleteNotification_success() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        String userIdHeader = userId.toString();
        Notification notification = Notification.create(userId, NotificationType.CS_ANSWERED,
                "문의 답변", NotificationChannel.SLACK);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(userId);
            given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                    .willReturn(Optional.of(notification));

            service.deleteNotification(notificationId, userIdHeader);

            assertThat(notification.isDeleted()).isTrue();
        }
    }

    @Test
    @DisplayName("deleteNotification - 본인 알림이 아니면 NOTIFICATION_403 예외가 발생한다")
    void deleteNotification_accessDenied() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        String userIdHeader = requesterId.toString();
        Notification notification = Notification.create(ownerId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            mock.when(() -> UserRoleCheck.parseUserId(userIdHeader)).thenReturn(requesterId);
            mock.when(() -> UserRoleCheck.requireOwner(requesterId, ownerId))
                    .thenThrow(new NotificationException(
                            com.ezmeal.notification.domain.exception.NotificationErrorCode.ACCESS_DENIED));
            given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                    .willReturn(Optional.of(notification));

            assertThatThrownBy(() -> service.deleteNotification(notificationId, userIdHeader))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("접근 권한이 없습니다");
        }
    }

    @Test
    @DisplayName("sendAdminNotification - 단일 유저 발송 시 sentCount=1을 반환한다")
    void sendAdminNotification_singleUser() {
        UUID targetUserId = UUID.randomUUID();
        AdminNotificationRequest request = mock(AdminNotificationRequest.class);
        given(request.getUserId()).willReturn(targetUserId);
        given(request.getMessage()).willReturn("공지사항입니다.");
        given(request.getChannel()).willReturn(NotificationChannel.EMAIL);
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            int result = service.sendAdminNotification(request, "MASTER");

            assertThat(result).isEqualTo(1);
            verify(notificationRouter, times(1)).route(any());
        }
    }

    @Test
    @DisplayName("sendAdminNotification - userId=null(전체 발송 미구현)이면 sentCount=0을 반환한다")
    void sendAdminNotification_allUsers_todoReturnsZero() {
        AdminNotificationRequest request = mock(AdminNotificationRequest.class);
        given(request.getUserId()).willReturn(null);

        try (MockedStatic<UserRoleCheck> mock = mockStatic(UserRoleCheck.class)) {
            int result = service.sendAdminNotification(request, "MASTER");

            assertThat(result).isEqualTo(0);
            verify(notificationRouter, never()).route(any());
        }
    }
}
