package com.ezmeal.notification.domain.event.consumer;

import com.ezmeal.common.message.inbox.InboxProcessor;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationDispatch;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.ezmeal.notification.domain.repository.NotificationRepository;
import com.ezmeal.notification.infrastructure.persistence.NotificationDispatchRepository;
import com.ezmeal.notification.infrastructure.saga.NotificationSagaDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Slf4j
public abstract class AbstractNotificationConsumer {

    // 추상 클래스는 @RequiredArgsConstructor 사용 불가 → @Autowired 필드 주입
    @Autowired
    private InboxProcessor inboxProcessor;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDispatchRepository dispatchRepository;

    @Autowired
    private NotificationSagaDispatcher sagaDispatcher;

    /**
     * 공통 이벤트 처리 흐름:
     * 1. InboxProcessor로 중복 수신 방지 + 단일 트랜잭션 보장
     * 2. 트랜잭션 커밋 후 즉시 발송 시도 (SagaDispatcher)
     *
     * eventId: EventEnvelope.eventId() — String 타입 (UUID 아님)
     */
    protected void handleEvent(String eventId,
                                UUID userId,
                                NotificationType type,
                                String message,
                                NotificationChannel channel) {

        // 람다 밖으로 저장된 Notification을 전달하기 위한 배열 참조
        // (람다는 effectively final 변수만 캡처 가능)
        Notification[] ref = new Notification[1];

        inboxProcessor.processOnce(eventId, () -> {
            Notification notification = notificationRepository.save(
                    Notification.create(userId, type, message, channel));
            dispatchRepository.save(NotificationDispatch.pending(notification, channel));
            ref[0] = notification;
        });

        // 중복 이벤트면 ref[0]이 null — 즉시 발송 생략
        if (ref[0] != null) {
            sagaDispatcher.dispatch(ref[0]);
        } else {
            log.info("[INBOX-DUPLICATE] 중복 이벤트 skip, eventId={}", eventId);
        }
    }
}
