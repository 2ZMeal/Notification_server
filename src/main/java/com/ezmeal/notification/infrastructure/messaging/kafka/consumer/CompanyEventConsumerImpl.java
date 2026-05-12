package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.CompanyEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.CompanyCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CompanyDeletedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CompanyEventConsumerImpl extends AbstractNotificationConsumer
        implements CompanyEventConsumer {

    @KafkaListener(topics = "company.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCompanyCreated(EventEnvelope<CompanyCreatedPayload> event) {
        CompanyCreatedPayload payload = event.payload();
        String msg = "업체 '" + payload.getCompanyName() + "' 이(가) 등록되었습니다.";
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.COMPANY_CREATED, msg, NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "company.deleted", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCompanyDeleted(EventEnvelope<CompanyDeletedPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.COMPANY_DELETED, "업체가 삭제되었습니다.", NotificationChannel.SLACK);
    }
}
