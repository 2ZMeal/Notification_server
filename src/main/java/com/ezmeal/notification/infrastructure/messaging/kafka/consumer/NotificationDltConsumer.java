package com.ezmeal.notification.infrastructure.messaging.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationDltConsumer {

    @KafkaListener(
            topics = {
                    "user.created.DLT",
                    "order.status.changed.DLT", "order.completed.DLT",
                    "payment.completed.DLT", "payment.failed.DLT", "payment.cancelled.DLT",
                    "company.created.DLT", "company.deleted.DLT",
                    "product.created.DLT", "product.deleted.DLT", "dailyMenu.created.DLT",
                    "cs.created.DLT", "cs.updated.DLT", "cs.answered.DLT",
                    "shipment.started.DLT", "shipment.delivered.DLT"
            },
            groupId = "${spring.kafka.consumer.group-id}-dlt",
            containerFactory = "kafkaDltListenerContainerFactory"
    )
    public void handleDlt(ConsumerRecord<String, String> record) {
        log.error("[DLT] 메시지 소비 최종 실패 | topic={}, offset={}, payload={}",
                record.topic(), record.offset(), record.value());
    }
}
