package com.ezmeal.notification.domain.entity;

public enum DispatchStatus {
    PENDING,  // 저장 완료, 발송 대기
    SENT,     // 발송 성공
    FAILED,   // 발송 실패 (SagaRetryWorker 재시도 대상)
    DEAD      // 최대 재시도 초과, 폐기
}
