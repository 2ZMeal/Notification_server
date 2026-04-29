package com.ezmeal.notification.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    // 단일 유저 이메일 조회 (Email 발송용)
    @GetMapping("/api/v1/internal/users/{userId}/email")
    String getUserEmail(@PathVariable("userId") UUID userId);

    // TODO: 전체 유저 대상 Admin 브로드캐스트는 추후 Kafka 이벤트 방식(admin.broadcast 토픽)으로 구현 예정
    //       현재 전체 유저 ID 목록 조회는 OOM/타임아웃 위험으로 주석 처리
    // @GetMapping("/api/v1/internal/users/ids")
    // List<UUID> getAllUserIds();
}
