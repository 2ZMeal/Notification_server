package com.ezmeal.notification.infrastructure.persistence;

import com.ezmeal.notification.domain.entity.DispatchStatus;
import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationDispatch;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDispatchRepository extends JpaRepository<NotificationDispatch, UUID> {

    /**
     * NotificationSagaDispatcher에서 즉시 발송 후 상태 업데이트 시 사용
     */
    Optional<NotificationDispatch> findByNotification(Notification notification);


    /**
     *  SagaRetryWorker에서 재시도 대상 조회 시 사용
     *  발송상태가 FAILED 이면서 최대시도횟수(3회) 미만인 row를 오래된 순으로 조회합니다.
     *  FOR UPDATE SKIP LOCKED
     * */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            SELECT d FROM NotificationDispatch d
            WHERE d.status = :#{T(com.ezmeal.notification.domain.entity.DispatchStatus).FAILED}
              AND d.attemptCount < 3
            ORDER BY d.lastAttemptedAt ASC
            """)
    List<NotificationDispatch> findRetryable();
}
