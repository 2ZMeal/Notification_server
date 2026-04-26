package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.OrderEventConsumer;
import com.ezmeal.notification.domain.event.payload.OrderReviewedPayload;
import com.ezmeal.notification.domain.event.payload.OrderStatusPayload;
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
public class OrderEventConsumerImpl implements OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "order.status", groupId = "notification-service")
    @Override
    public void onOrderStatus(String message) {
        try {
            OrderStatusPayload payload = objectMapper.readValue(message, OrderStatusPayload.class);
            String msg = "주문 상태가 변경되었습니다: " + payload.getStatus();
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.ORDER_STATUS_CHANGED,
                    msg, NotificationChannel.EMAIL
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] order.status 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "order.reviewed", groupId = "notification-service")
    @Override
    public void onOrderReviewed(String message) {
        try {
            OrderReviewedPayload payload = objectMapper.readValue(message, OrderReviewedPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.ORDER_REVIEWED,
                    "주문에 대한 리뷰를 남겨주세요.", NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] order.reviewed 처리 실패: {}", e.getMessage());
        }
    }
}
