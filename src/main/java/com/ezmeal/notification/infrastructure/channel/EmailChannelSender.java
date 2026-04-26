package com.ezmeal.notification.infrastructure.channel;

import com.ezmeal.notification.domain.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannelSender implements ChannelSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    public void send(Notification notification, String recipientEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(recipientEmail);
        msg.setSubject("[EzMeal] " + notification.getType().name());
        msg.setText(notification.getMessage());
        mailSender.send(msg);
        notification.updateSentAt(LocalDateTime.now());
        log.info("[EMAIL-SENT] to={}, type={}", recipientEmail, notification.getType());
    }
}
