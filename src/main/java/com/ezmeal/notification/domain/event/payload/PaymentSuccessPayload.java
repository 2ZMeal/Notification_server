package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ezmeal.common.message.DomainEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentSuccessPayload implements DomainEvent {
    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private Long amount;
}
