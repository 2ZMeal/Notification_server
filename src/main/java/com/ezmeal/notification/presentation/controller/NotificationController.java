package com.ezmeal.notification.presentation.controller;

import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.application.service.NotificationApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService service;

    // 알림 목록 조회
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        List<NotificationResponse> result = service.getNotifications(userId, role);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림 목록 조회 성공",
                "data", result
        ));
    }

    // 단건 알림 조회 (조회 시 자동 읽음 처리)
    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<?> getNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("X-User-Id") String userId) {
        NotificationResponse result = service.getNotification(notificationId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림 조회 성공",
                "data", result
        ));
    }

    // 알림 소프트 삭제
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("X-User-Id") String userId) {
        service.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림이 삭제되었습니다.",
                "data", null
        ));
    }

    // 어드민 수동 발송 (MASTER 전용)
    @PostMapping("/admin/notifications")
    public ResponseEntity<?> sendAdminNotification(
            @RequestBody @Valid AdminNotificationRequest request,
            @RequestHeader("X-User-Role") String role) {
        int sentCount = service.sendAdminNotification(request, role);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림 발송 완료",
                "data", Map.of("sentCount", sentCount)
        ));
    }
}
