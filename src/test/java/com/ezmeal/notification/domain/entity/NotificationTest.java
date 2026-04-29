package com.ezmeal.notification.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("create() - 초기 상태는 isRead=false, sentAt=null, deletedAt=null")
    void create_initialState() {
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.create(
                userId, NotificationType.SHIPMENT_STARTED,
                "배송이 시작되었습니다.", NotificationChannel.EMAIL
        );

        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getType()).isEqualTo(NotificationType.SHIPMENT_STARTED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getSentAt()).isNull();
        assertThat(notification.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("markAsRead() - isRead가 true로 변경된다")
    void markAsRead() {
        Notification notification = Notification.create(
                UUID.randomUUID(), NotificationType.PAYMENT_SUCCESS,
                "결제 완료", NotificationChannel.EMAIL
        );

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("softDelete() - deletedAt과 deletedBy가 설정된다")
    void softDelete() {
        Notification notification = Notification.create(
                UUID.randomUUID(), NotificationType.ORDER_STATUS_CHANGED,
                "주문 상태 변경", NotificationChannel.EMAIL
        );
        String deletedBy = UUID.randomUUID().toString();

        notification.softDelete(deletedBy);

        assertThat(notification.getDeletedAt()).isNotNull();
        assertThat(notification.getDeletedBy()).isEqualTo(deletedBy);
    }

    @Test
    @DisplayName("isDeleted() - deletedAt이 있을 때 true를 반환한다")
    void isDeleted() {
        Notification notification = Notification.create(
                UUID.randomUUID(), NotificationType.CS_ANSWERED,
                "문의 답변 완료", NotificationChannel.SLACK
        );

        assertThat(notification.isDeleted()).isFalse();

        notification.softDelete("system");

        assertThat(notification.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("updateSentAt() - sentAt이 설정된다")
    void updateSentAt() {
        Notification notification = Notification.create(
                UUID.randomUUID(), NotificationType.SHIPMENT_DELIVERED,
                "배송 완료", NotificationChannel.EMAIL
        );
        LocalDateTime sentTime = LocalDateTime.now();

        notification.updateSentAt(sentTime);

        assertThat(notification.getSentAt()).isEqualTo(sentTime);
    }
}
