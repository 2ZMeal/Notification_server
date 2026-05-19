package com.ezmeal.notification.domain.entity;

import com.ezmeal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime sentAt;

    public static Notification create(UUID userId, NotificationType type,
                                      String message, NotificationChannel channel) {
        Notification n = new Notification();
        n.userId = userId;
        n.type = type;
        n.message = message;
        n.channel = channel;
        n.setSystemCreated();
        return n;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void updateSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void softDelete(String deletedBy) {
        super.delete(deletedBy);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
