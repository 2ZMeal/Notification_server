package com.ezmeal.notification.infrastructure.channel;

import com.ezmeal.notification.domain.entity.Notification;

public interface ChannelSender {
    void send(Notification notification, String recipientEmail);
}
