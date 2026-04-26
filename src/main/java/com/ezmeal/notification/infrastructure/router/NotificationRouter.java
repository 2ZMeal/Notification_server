package com.ezmeal.notification.infrastructure.router;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.infrastructure.channel.EmailChannelSender;
import com.ezmeal.notification.infrastructure.channel.SlackChannelSender;
import com.ezmeal.notification.infrastructure.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRouter {

    private static final Set<NotificationType> EMAIL_TYPES = Set.of(
            NotificationType.SHIPMENT_STARTED,
            NotificationType.SHIPMENT_DELIVERED,
            NotificationType.ORDER_STATUS_CHANGED,
            NotificationType.PAYMENT_SUCCESS,
            NotificationType.PAYMENT_CANCELLED,
            NotificationType.ADMIN_BROADCAST
    );

    private final EmailChannelSender emailSender;
    private final SlackChannelSender slackSender;
    private final UserClient userClient;

    public void route(Notification notification) {
        if (EMAIL_TYPES.contains(notification.getType())) {
            try {
                String email = userClient.getUserEmail(notification.getUserId());
                emailSender.send(notification, email);
            } catch (Exception e) {
                log.error("[EMAIL-FAIL] userId={}, type={}, error={}",
                        notification.getUserId(), notification.getType(), e.getMessage());
                // Email 실패해도 DB 저장은 보장 (sentAt = null 유지)
            }
        } else {
            slackSender.send(notification, null);
        }
    }
}
