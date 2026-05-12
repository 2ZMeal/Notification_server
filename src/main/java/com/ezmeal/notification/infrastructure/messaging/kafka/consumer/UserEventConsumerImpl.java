package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.UserEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.UserCreatedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumerImpl extends AbstractNotificationConsumer
        implements UserEventConsumer {

    @KafkaListener(topics = "user.created", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onUserCreated(EventEnvelope<UserCreatedPayload> event) {
        UserCreatedPayload payload = event.payload();
        String msg = "환영합니다, " + payload.getUsername() + "님! 회원가입이 완료되었습니다.";
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.USER_CREATED, msg, NotificationChannel.SLACK);
    }
}
