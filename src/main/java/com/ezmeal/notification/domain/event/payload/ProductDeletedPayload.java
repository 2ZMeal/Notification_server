package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ProductDeletedPayload {
    private UUID eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private UUID productId;
    private UUID userId;
}
