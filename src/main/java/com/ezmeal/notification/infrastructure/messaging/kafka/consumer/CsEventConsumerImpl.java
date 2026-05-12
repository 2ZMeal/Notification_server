package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.CsEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.CsAnsweredPayload;
import com.ezmeal.notification.domain.event.payload.CsCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CsUpdatedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsEventConsumerImpl extends AbstractNotificationConsumer
        implements CsEventConsumer {

    @KafkaListener(topics = "cs.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCsCreated(EventEnvelope<CsCreatedPayload> event) {
        CsCreatedPayload payload = event.payload();
        String msg = "문의가 접수되었습니다: " + payload.getTitle();
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.CS_CREATED, msg, NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "cs.updated", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCsUpdated(EventEnvelope<CsUpdatedPayload> event) {
        CsUpdatedPayload payload = event.payload();
        String msg = "문의가 수정되었습니다: " + payload.getTitle();
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.CS_UPDATED, msg, NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "cs.answered", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onCsAnswered(EventEnvelope<CsAnsweredPayload> event) {
        handleEvent(event.eventId(), event.payload().getUserId(),
                NotificationType.CS_ANSWERED, "문의에 답변이 등록되었습니다.", NotificationChannel.SLACK);
    }
}
