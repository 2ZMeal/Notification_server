package com.ezmeal.notification.application.service;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.client.UserClient;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(UUID userId, Role role) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId.toString(), role);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("getNotifications - 본인 알림 목록을 반환한다")
    void getNotifications_success() {
        UUID userId = UUID.randomUUID();
        mockSecurityContext(userId, Role.USER);
        Notification notification = Notification.create(userId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);
        given(notificationRepository.findAllByUserIdAndDeletedAtIsNull(userId))
                .willReturn(List.of(notification));

        List<NotificationResponse> result = service.getNotifications();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getNotification - 단건 조회 시 isRead가 true로 변경된다")
    void getNotification_marksAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        mockSecurityContext(userId, Role.USER);
        Notification notification = Notification.create(userId, NotificationType.PAYMENT_SUCCESS,
                "결제 완료", NotificationChannel.EMAIL);
        given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                .willReturn(Optional.of(notification));

        NotificationResponse result = service.getNotification(notificationId);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    @DisplayName("getNotification - 본인 알림이 아니면 ForbiddenException이 발생한다")
    void getNotification_accessDenied() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        mockSecurityContext(requesterId, Role.USER);
        Notification notification = Notification.create(ownerId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);
        given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                .willReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.getNotification(notificationId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("getNotification - 존재하지 않는 알림이면 NotFoundException이 발생한다")
    void getNotification_notFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        mockSecurityContext(userId, Role.USER);
        given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getNotification(notificationId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteNotification - 정상적으로 소프트 삭제된다")
    void deleteNotification_success() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        mockSecurityContext(userId, Role.USER);
        Notification notification = Notification.create(userId, NotificationType.CS_ANSWERED,
                "문의 답변", NotificationChannel.SLACK);
        given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                .willReturn(Optional.of(notification));

        service.deleteNotification(notificationId);

        assertThat(notification.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("deleteNotification - 본인 알림이 아니면 ForbiddenException이 발생한다")
    void deleteNotification_accessDenied() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        mockSecurityContext(requesterId, Role.USER);
        Notification notification = Notification.create(ownerId, NotificationType.SHIPMENT_STARTED,
                "배송 시작", NotificationChannel.EMAIL);
        given(notificationRepository.findByIdAndDeletedAtIsNull(notificationId))
                .willReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.deleteNotification(notificationId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("sendAdminNotification - 단일 유저 발송 시 sentCount=1을 반환한다")
    void sendAdminNotification_singleUser() {
        UUID targetUserId = UUID.randomUUID();
        mockSecurityContext(UUID.randomUUID(), Role.ADMIN);
        AdminNotificationRequest request = mock(AdminNotificationRequest.class);
        given(request.getUserId()).willReturn(targetUserId);
        given(request.getMessage()).willReturn("공지사항입니다.");
        given(request.getChannel()).willReturn(NotificationChannel.EMAIL);
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        int result = service.sendAdminNotification(request);

        assertThat(result).isEqualTo(1);
        verify(notificationRouter, times(1)).route(any());
    }

    @Test
    @DisplayName("sendAdminNotification - userId=null(전체 발송 미구현)이면 sentCount=0을 반환한다")
    void sendAdminNotification_allUsers_todoReturnsZero() {
        mockSecurityContext(UUID.randomUUID(), Role.ADMIN);
        AdminNotificationRequest request = mock(AdminNotificationRequest.class);
        given(request.getUserId()).willReturn(null);

        int result = service.sendAdminNotification(request);

        assertThat(result).isEqualTo(0);
        verify(notificationRouter, never()).route(any());
    }
}
