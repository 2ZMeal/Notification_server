package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.CsEventConsumer;
import com.ezmeal.notification.domain.event.payload.CsAnsweredPayload;
import com.ezmeal.notification.domain.event.payload.CsCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CsUpdatedPayload;
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
public class CsEventConsumerImpl implements CsEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "cs.created", groupId = "notification-service")
    @Override
    public void onCsCreated(String message) {
        try {
            CsCreatedPayload payload = objectMapper.readValue(message, CsCreatedPayload.class);
            String msg = "문의가 접수되었습니다: " + payload.getTitle();
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.CS_CREATED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] cs.created 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "cs.updated", groupId = "notification-service")
    @Override
    public void onCsUpdated(String message) {
        try {
            CsUpdatedPayload payload = objectMapper.readValue(message, CsUpdatedPayload.class);
            String msg = "문의가 수정되었습니다: " + payload.getTitle();
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.CS_UPDATED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] cs.updated 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "cs.answered", groupId = "notification-service")
    @Override
    public void onCsAnswered(String message) {
        try {
            CsAnsweredPayload payload = objectMapper.readValue(message, CsAnsweredPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.CS_ANSWERED,
                    "문의에 답변이 등록되었습니다.", NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] cs.answered 처리 실패: {}", e.getMessage());
        }
    }
}
