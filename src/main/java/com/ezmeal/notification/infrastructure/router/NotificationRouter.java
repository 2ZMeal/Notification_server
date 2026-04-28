package com.ezmeal.notification.infrastructure.router;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.infrastructure.channel.EmailChannelSender;
import com.ezmeal.notification.infrastructure.channel.SlackChannelSender;
import com.ezmeal.notification.infrastructure.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    // TODO: [테스트 전용] 로컬 환경에서 user-service 없이 이메일 발송 검증용
    //       운영 배포 전 반드시 제거할 것:
    //       1. 이 @Value 필드 삭제
    //       2. route() 내 삼항 연산자를 userClient.getUserEmail(...)으로 단순화
    //       3. application.yaml의 notification.test.target-email 설정 제거
    @Value("${notification.test.target-email:}")
    private String testTargetEmail;

    public void route(Notification notification) {
        if (EMAIL_TYPES.contains(notification.getType())) {
            try {
                // TODO: [테스트 전용] 운영 시 아래 한 줄로 교체
                //       String email = userClient.getUserEmail(notification.getUserId());
                String email = StringUtils.hasText(testTargetEmail)
                        ? testTargetEmail
                        : userClient.getUserEmail(notification.getUserId());
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
