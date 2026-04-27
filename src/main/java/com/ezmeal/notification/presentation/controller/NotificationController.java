package com.ezmeal.notification.presentation.controller;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.notification.application.dto.request.AdminNotificationRequest;
import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.application.service.NotificationApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService service;

    // 알림 목록 조회
    @GetMapping("/notifications")
    public ResponseEntity<CommonApiResponse<List<NotificationResponse>>> getNotifications() {
        return ResponseEntity.ok(CommonApiResponse.success("알림 목록 조회 성공", service.getNotifications()));
    }

    // 단건 알림 조회 (조회 시 자동 읽음 처리)
    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<CommonApiResponse<NotificationResponse>> getNotification(
            @PathVariable UUID notificationId) {
        return ResponseEntity.ok(CommonApiResponse.success("알림 조회 성공", service.getNotification(notificationId)));
    }

    // 알림 소프트 삭제
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteNotification(
            @PathVariable UUID notificationId) {
        service.deleteNotification(notificationId);
        return ResponseEntity.ok(CommonApiResponse.success("알림이 삭제되었습니다.", null));
    }

    // 어드민 수동 발송 (MASTER 전용)
    @PostMapping("/admin/notifications")
    public ResponseEntity<CommonApiResponse<Integer>> sendAdminNotification(
            @RequestBody @Valid AdminNotificationRequest request) {
        int sentCount = service.sendAdminNotification(request);
        return ResponseEntity.ok(CommonApiResponse.success("알림 발송 완료", sentCount));
    }
}
