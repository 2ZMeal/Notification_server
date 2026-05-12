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
// [테스트 전용] local 프로필 이메일 우회 시 활성화
// import jakarta.annotation.PostConstruct;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.env.Environment;
// import org.springframework.util.StringUtils;
// import java.util.Arrays;

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

    /**
     * 알림 타입 기반으로 채널 결정 후 발송
     * 예외를 NotificationSagaDispatcher 로 전파(FAILED 로 마킹)
     * */
    public void route(Notification notification) {
        if (EMAIL_TYPES.contains(notification.getType())) {
            String email = userClient.getUser(notification.getUserId()).getData().getEmail();
            emailSender.send(notification, email);
        } else {
            slackSender.send(notification, null);
        }
    }

    // Saga fallback 전용 — 타입과 무관하게 강제 Slack 발송
    public void routeToSlack(Notification notification) {
        slackSender.send(notification, null);
    }

}
