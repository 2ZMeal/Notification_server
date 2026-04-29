package com.ezmeal.notification.infrastructure.channel;

import com.ezmeal.notification.domain.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SlackChannelSender implements ChannelSender {

    @Override
    public void send(Notification notification, String recipientEmail) {
        log.info("[SLACK-SIMULATED] userId={}, type={}, message={}",
                notification.getUserId(), notification.getType(), notification.getMessage());
        notification.updateSentAt(LocalDateTime.now());
    }
}
