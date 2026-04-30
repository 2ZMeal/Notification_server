package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.CompanyEventConsumer;
import com.ezmeal.notification.domain.event.payload.CompanyCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CompanyDeletedPayload;
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
public class CompanyEventConsumerImpl implements CompanyEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "company.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCompanyCreated(String message) {
        try {
            CompanyCreatedPayload payload = objectMapper.readValue(message, CompanyCreatedPayload.class);
            String msg = "업체 '" + payload.getCompanyName() + "' 이(가) 등록되었습니다.";
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.COMPANY_CREATED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] company.created 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "company.deleted", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCompanyDeleted(String message) {
        try {
            CompanyDeletedPayload payload = objectMapper.readValue(message, CompanyDeletedPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.COMPANY_DELETED,
                    "업체가 삭제되었습니다.", NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] company.deleted 처리 실패: {}", e.getMessage());
        }
    }
}
