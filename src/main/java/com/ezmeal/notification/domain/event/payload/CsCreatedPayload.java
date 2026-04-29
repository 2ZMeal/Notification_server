package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CsCreatedPayload {
    private UUID eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private UUID csId;
    private UUID userId;
    private String title;
}
