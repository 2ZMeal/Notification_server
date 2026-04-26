package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentCancelledPayload {
    private UUID eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private Long amount;
}
