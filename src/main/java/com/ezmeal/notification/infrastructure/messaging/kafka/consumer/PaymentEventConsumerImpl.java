package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.event.PaymentEventConsumer;
import com.ezmeal.notification.domain.event.payload.PaymentCancelledPayload;
import com.ezmeal.notification.domain.event.payload.PaymentFailedPayload;
import com.ezmeal.notification.domain.event.payload.PaymentSuccessPayload;
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
public class PaymentEventConsumerImpl implements PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationRouter notificationRouter;

    @KafkaListener(topics = "payment.success", groupId = "notification-service")
    @Override
    public void onPaymentSuccess(String message) {
        try {
            PaymentSuccessPayload payload = objectMapper.readValue(message, PaymentSuccessPayload.class);
            String msg = "결제가 완료되었습니다. 금액: " + payload.getAmount() + "원";
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.PAYMENT_SUCCESS,
                    msg, NotificationChannel.EMAIL
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] payment.success 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    @Override
    public void onPaymentFailed(String message) {
        try {
            PaymentFailedPayload payload = objectMapper.readValue(message, PaymentFailedPayload.class);
            String msg = "결제에 실패하였습니다. 사유: " + payload.getReason();
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.PAYMENT_FAILED,
                    msg, NotificationChannel.SLACK
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] payment.failed 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.cancelled", groupId = "notification-service")
    @Override
    public void onPaymentCancelled(String message) {
        try {
            PaymentCancelledPayload payload = objectMapper.readValue(message, PaymentCancelledPayload.class);
            String msg = "결제가 취소되었습니다. 환불 금액: " + payload.getAmount() + "원";
            Notification notification = Notification.create(
                    payload.getUserId(), NotificationType.PAYMENT_CANCELLED,
                    msg, NotificationChannel.EMAIL
            );
            notificationRepository.save(notification);
            notificationRouter.route(notification);
        } catch (Exception e) {
            log.error("[KAFKA-ERROR] payment.cancelled 처리 실패: {}", e.getMessage());
        }
    }
}
