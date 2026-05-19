package com.ezmeal.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_notification_dispatch")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 발송 대상 알림 — LAZY 로딩으로 불필요한 JOIN 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private int attemptCount = 0;

    private LocalDateTime lastAttemptedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // 낙관적 락 — 다중 인스턴스 환경에서 동일 row 동시 수정 충돌 감지
    @Version
    private Long version = 0L;

    // ── 팩토리 메서드 ───────────────────────────────────────────────────────────

    public static NotificationDispatch pending(Notification notification, NotificationChannel channel) {
        NotificationDispatch d = new NotificationDispatch();
        d.notification = notification;
        d.channel = channel;
        d.status = DispatchStatus.PENDING;
        return d;
    }

    // ── 상태 전이 메서드 ────────────────────────────────────────────────────────

    public void markSent() {
        this.status = DispatchStatus.SENT;
        this.lastAttemptedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = DispatchStatus.FAILED;
        this.attemptCount++;
        this.lastAttemptedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void markDead(String errorMessage) {
        this.status = DispatchStatus.DEAD;
        this.lastAttemptedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void changeChannelToSlack() {
        this.channel = NotificationChannel.SLACK;
    }

    // ── 조건 메서드 ─────────────────────────────────────────────────────────────

    public boolean isMaxAttemptReached() {
        return this.attemptCount >= 3;
    }
}
