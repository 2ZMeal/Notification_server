package com.ezmeal.notification.infrastructure.saga;

import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationDispatch;
import com.ezmeal.notification.infrastructure.persistence.NotificationDispatchRepository;
import com.ezmeal.notification.infrastructure.router.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaRetryWorker {

    private final NotificationDispatchRepository dispatchRepository;
    private final NotificationRouter notificationRouter;

    /**
     * 5분마다 FAILED 상태 메시지 재시도(300,000ms)
     * @Transcational의 이유
     * 1. PESSIMISTIC_WRITE를 유지하기 위해서
     * 2. DirtyChecking으로 상태 업데이트가 자동으로 이루어지게하기 위함
     * */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void retryFailed() {
        List<NotificationDispatch> targets = dispatchRepository.findRetryable();

        if (targets.isEmpty()) return;

        log.info("[SAGA-RETRY] 재시도 대상 {}건 처리 시작", targets.size());

        for (NotificationDispatch dispatch : targets) {
            if (dispatch.isMaxAttemptReached()) {
                handleMaxAttemptExceeded(dispatch);
            } else {
                retryDispatch(dispatch);
            }
        }
    }

    private void retryDispatch(NotificationDispatch dispatch) {
        try {
            notificationRouter.route(dispatch.getNotification());
            dispatch.markSent();
            log.info("[SAGA-RETRY-SENT] notificationId={}", dispatch.getNotification().getId());
        } catch (Exception e) {
            dispatch.markFailed(e.getMessage());
            log.warn("[SAGA-RETRY-FAILED] notificationId={}, attempt={}, error={}",
                    dispatch.getNotification().getId(), dispatch.getAttemptCount(), e.getMessage());
        }
    }

    private void handleMaxAttemptExceeded(NotificationDispatch dispatch) {
        if (dispatch.getChannel() == NotificationChannel.EMAIL) {
            fallbackToSlack(dispatch);
        } else {
            dispatch.markDead("최대 재시도 횟수 초과");
            log.error("[SAGA-DEAD] notificationId={}", dispatch.getNotification().getId());
        }
    }

    // EMAIL 3회 실패 시 SLACK으로 채널 변경 후 재시도
    private void fallbackToSlack(NotificationDispatch dispatch) {
        dispatch.changeChannelToSlack();
        try {
            notificationRouter.routeToSlack(dispatch.getNotification());
            dispatch.markSent();
            log.info("[SAGA-FALLBACK-SENT] notificationId={} EMAIL→SLACK 성공",
                    dispatch.getNotification().getId());
        } catch (Exception e) {
            dispatch.markDead(e.getMessage());
            log.error("[SAGA-DEAD] notificationId={} SLACK fallback 실패",
                    dispatch.getNotification().getId());
        }
    }
}
