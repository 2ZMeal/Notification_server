package com.ezmeal.notification.application.dto.request;

import com.ezmeal.notification.domain.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class AdminNotificationRequest {

    private UUID userId;    // nullable (null = 전체 발송)

    @NotBlank
    private String message;

    @NotNull
    private NotificationChannel channel;
}
