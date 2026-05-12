package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.ProductEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.DailyMenuCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductDeletedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventConsumerImpl extends AbstractNotificationConsumer
        implements ProductEventConsumer {

    @KafkaListener(topics = "product.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onProductCreated(EventEnvelope<ProductCreatedPayload> event) {
        ProductCreatedPayload payload = event.payload();
        String msg = "새 상품 '" + payload.getProductName() + "' 이(가) 등록되었습니다.";
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.PRODUCT_CREATED, msg, NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "product.deleted", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onProductDeleted(EventEnvelope<ProductDeletedPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.PRODUCT_DELETED, "상품이 삭제되었습니다.", NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "dailyMenu.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onDailyMenuCreated(EventEnvelope<DailyMenuCreatedPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.DAILY_MENU_CREATED, "새로운 요일별 식단이 등록되었습니다.", NotificationChannel.SLACK);
    }
}
