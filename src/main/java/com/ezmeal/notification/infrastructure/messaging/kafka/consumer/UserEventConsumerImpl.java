package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.UserEventConsumer;
import com.ezmeal.notification.domain.event.payload.UserCreatedPayload;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumerImpl implements UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "user.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onUserCreated(String message) {
        try {
            UserCreatedPayload payload = objectMapper.readValue(message, UserCreatedPayload.class);
            String msg = "환영합니다, " + payload.getUsername() + "님! 회원가입이 완료되었습니다.";
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.USER_CREATED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] user.created 처리 실패: {}", e.getMessage());
        }
    }
}
