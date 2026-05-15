package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.OrderEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.OrderReviewedPayload;
import com.ezmeal.notification.domain.event.payload.OrderStatusPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumerImpl extends AbstractNotificationConsumer
        implements OrderEventConsumer {

    @KafkaListener(topics = "order.status.changed", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onOrderStatus(EventEnvelope<OrderStatusPayload> event) {
        OrderStatusPayload payload = event.payload();
        String msg = "주문 상태가 변경되었습니다: " + payload.getStatus();
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.ORDER_STATUS_CHANGED, msg, NotificationChannel.EMAIL);
    }

    @KafkaListener(topics = "order.completed", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onOrderReviewed(EventEnvelope<OrderReviewedPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.ORDER_REVIEWED, "주문에 대한 리뷰를 남겨주세요.", NotificationChannel.SLACK);
    }
}
