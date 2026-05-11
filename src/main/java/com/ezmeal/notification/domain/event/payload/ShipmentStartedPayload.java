package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ezmeal.common.message.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ShipmentStartedPayload implements DomainEvent {
    private UUID shipmentId;
    private UUID orderId;
    private UUID userId;
    private String trackingNumber;
    private LocalDateTime startedAt;
}
