package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.ProductEventConsumer;
import com.ezmeal.notification.domain.event.payload.DailyMenuCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductDeletedPayload;
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
public class ProductEventConsumerImpl implements ProductEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "product.created", groupId = "notification-service")
    @Override
    public void onProductCreated(String message) {
        try {
            ProductCreatedPayload payload = objectMapper.readValue(message, ProductCreatedPayload.class);
            String msg = "새 상품 '" + payload.getProductName() + "' 이(가) 등록되었습니다.";
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.PRODUCT_CREATED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] product.created 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "product.deleted", groupId = "notification-service")
    @Override
    public void onProductDeleted(String message) {
        try {
            ProductDeletedPayload payload = objectMapper.readValue(message, ProductDeletedPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.PRODUCT_DELETED,
                    "상품이 삭제되었습니다.", NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] product.deleted 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "dailyMenu.created", groupId = "notification-service")
    @Override
    public void onDailyMenuCreated(String message) {
        try {
            DailyMenuCreatedPayload payload = objectMapper.readValue(message, DailyMenuCreatedPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.DAILY_MENU_CREATED,
                    "새로운 요일별 식단이 등록되었습니다.", NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] dailyMenu.created 처리 실패: {}", e.getMessage());
        }
    }
}
