package com.ezmeal.notification.infrastructure.persistence;

import com.ezmeal.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdAndDeletedAtIsNull(UUID id);
    List<Notification> findAllByUserIdAndDeletedAtIsNull(UUID userId);
}
