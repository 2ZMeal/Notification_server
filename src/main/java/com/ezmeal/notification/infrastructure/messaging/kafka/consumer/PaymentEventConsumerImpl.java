package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.PaymentEventConsumer;
import com.ezmeal.notification.domain.event.consumer.AbstractNotificationConsumer;
import com.ezmeal.notification.domain.event.payload.PaymentCancelledPayload;
import com.ezmeal.notification.domain.event.payload.PaymentFailedPayload;
import com.ezmeal.notification.domain.event.payload.PaymentSuccessPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventConsumerImpl extends AbstractNotificationConsumer
        implements PaymentEventConsumer {

    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onPaymentSuccess(EventEnvelope<PaymentSuccessPayload> event) {
        PaymentSuccessPayload payload = event.payload();
        String msg = "결제가 완료되었습니다. 금액: " + payload.getAmount() + "원";
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.PAYMENT_SUCCESS, msg, NotificationChannel.EMAIL);
    }

    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onPaymentFailed(EventEnvelope<PaymentFailedPayload> event) {
        PaymentFailedPayload payload = event.payload();
        String msg = "결제에 실패하였습니다. 사유: " + payload.getReason();
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.PAYMENT_FAILED, msg, NotificationChannel.SLACK);
    }

    @KafkaListener(topics = "payment.cancelled", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onPaymentCancelled(EventEnvelope<PaymentCancelledPayload> event) {
        PaymentCancelledPayload payload = event.payload();
        String msg = "결제가 취소되었습니다. 환불 금액: " + payload.getAmount() + "원";
        handleEvent(event.eventId(), payload.getUserId(),
                NotificationType.PAYMENT_CANCELLED, msg, NotificationChannel.EMAIL);
    }
}
