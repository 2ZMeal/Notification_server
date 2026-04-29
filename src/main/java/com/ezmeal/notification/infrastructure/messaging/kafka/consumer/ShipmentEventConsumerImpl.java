package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.ShipmentEventConsumer;
import com.ezmeal.notification.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.notification.domain.event.payload.ShipmentStartedPayload;
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
public class ShipmentEventConsumerImpl implements ShipmentEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "shipment.started", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onShipmentStarted(String message) {
        try {
            ShipmentStartedPayload payload = objectMapper.readValue(message, ShipmentStartedPayload.class);
            String msg = "배송이 시작되었습니다. 운송장번호: " + payload.getTrackingNumber();
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.SHIPMENT_STARTED,
                    msg, NotificationChannel.EMAIL
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] shipment.started 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "shipment.delivered", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onShipmentDelivered(String message) {
        try {
            ShipmentDeliveredPayload payload = objectMapper.readValue(message, ShipmentDeliveredPayload.class);
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.SHIPMENT_DELIVERED,
                    "배송이 완료되었습니다.", NotificationChannel.EMAIL
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] shipment.delivered 처리 실패: {}", e.getMessage());
        }
    }
}
