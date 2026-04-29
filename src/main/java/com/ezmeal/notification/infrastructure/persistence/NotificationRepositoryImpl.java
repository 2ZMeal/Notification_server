package com.ezmeal.notification.infrastructure.persistence;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaRepo;

    @Override
    public Notification save(Notification notification) {
        return jpaRepo.save(notification);
    }

    @Override
    public Optional<Notification> findByIdAndDeletedAtIsNull(UUID id) {
        return jpaRepo.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<Notification> findAllByUserIdAndDeletedAtIsNull(UUID userId) {
        return jpaRepo.findAllByUserIdAndDeletedAtIsNull(userId);
    }
}
