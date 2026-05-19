package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.ShipmentEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.notification.domain.event.payload.ShipmentStartedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentEventConsumerImpl extends AbstractNotificationConsumer
        implements ShipmentEventConsumer {

    @KafkaListener(topics = "shipment.started", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onShipmentStarted(EventEnvelope<ShipmentStartedPayload> event) {
        ShipmentStartedPayload payload = event.payload();
        String msg = "배송이 시작되었습니다. 운송장번호: " + payload.getTrackingNumber();
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.SHIPMENT_STARTED, msg, NotificationChannel.EMAIL);
    }

    @KafkaListener(topics = "shipment.delivered", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onShipmentDelivered(EventEnvelope<ShipmentDeliveredPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.SHIPMENT_DELIVERED, "배송이 완료되었습니다.", NotificationChannel.EMAIL);
    }
}
