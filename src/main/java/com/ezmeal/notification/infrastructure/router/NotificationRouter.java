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
    // [테스트 전용] local 프로필 이메일 우회 시 활성화
    // private final Environment environment;
    // @Value("${notification.test.target-email:}")
    // private String testTargetEmail;
    // @PostConstruct
    // void validateTestEmailConfig() {
    //     if (StringUtils.hasText(testTargetEmail)) {
    //         boolean isSafeProfile = Arrays.stream(environment.getActiveProfiles())
    //                 .anyMatch(p -> p.equals("local") || p.equals("test"));
    //         if (!isSafeProfile) {
    //             throw new IllegalStateException(
    //                     "notification.test.target-email은 local/test 프로필에서만 허용됩니다.");
    //         }
    //     }
    // }

    public void route(Notification notification) {
        if (EMAIL_TYPES.contains(notification.getType())) {
            try {
                String email = userClient.getUser(notification.getUserId()).getData().getEmail();
                // [테스트 전용] local 프로필 이메일 우회 시 아래로 교체
                // String email = StringUtils.hasText(testTargetEmail)
                //         ? testTargetEmail
                //         : userClient.getUser(notification.getUserId()).getData().getEmail();
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
