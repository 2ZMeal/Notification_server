package com.ezmeal.notification.application.dto.response;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationResponse {

    private final UUID id;
    private final NotificationType type;
    private final String message;
    private final NotificationChannel channel;
    @JsonProperty("isRead")
    private final boolean isRead;
    private final LocalDateTime sentAt;
    private final LocalDateTime createdAt;

    private NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.channel = notification.getChannel();
        this.isRead = notification.isRead();
        this.sentAt = notification.getSentAt();
        this.createdAt = notification.getCreatedAt();
    }

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification);
    }
}
