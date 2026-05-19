package com.ezmeal.notification.infrastructure.saga;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationDispatch;
import com.ezmeal.notification.infrastructure.persistence.NotificationDispatchRepository;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSagaDispatcher {

    private final NotificationDispatchRepository dispatchRepository;
    private final NotificationRouter notificationRouter;

    /**
     * Inbox Transcation 커밋 후 호출
     * 발송성공 : SENT / 발송실패 : FAILED
     * */
    public void dispatch(Notification notification) {
        NotificationDispatch dispatch = dispatchRepository.findByNotification(notification)
                .orElseThrow(() -> new IllegalStateException(
                        "[SAGA] dispatch record not found for notificationId=" + notification.getId()));
        try {
            notificationRouter.route(notification);
            dispatch.markSent();
            log.info("[SAGA-SENT] notificationId={}, channel={}", notification.getId(), dispatch.getChannel());
        } catch (Exception e) {
            dispatch.markFailed(e.getMessage());
            log.warn("[SAGA-FAILED] notificationId={}, attempt={}, error={}",
                    notification.getId(), dispatch.getAttemptCount(), e.getMessage());
        }
        dispatchRepository.save(dispatch);
    }
}
