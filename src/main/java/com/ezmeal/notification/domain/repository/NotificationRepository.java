package com.ezmeal.notification.domain.repository;

import com.ezmeal.notification.domain.entity.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findByIdAndDeletedAtIsNull(UUID id);
    List<Notification> findAllByUserIdAndDeletedAtIsNull(UUID userId);
}
