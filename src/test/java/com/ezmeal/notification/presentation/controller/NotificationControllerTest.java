package com.ezmeal.notification.presentation.controller;

import com.ezmeal.notification.application.dto.response.NotificationResponse;
import com.ezmeal.notification.application.service.NotificationApplicationService;
import com.ezmeal.notification.domain.entity.NotificationChannel;
import com.ezmeal.notification.domain.entity.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@MockBean(JpaMetamodelMappingContext.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    NotificationApplicationService service;

    @Test
    @DisplayName("GET /api/v1/notifications - 200 OK + 목록 반환")
    void getNotifications_200() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationResponse response = createMockResponse(userId);
        given(service.getNotifications(any(), any())).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value("SHIPMENT_STARTED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{id} - 200 OK + isRead 필드 포함")
    void getNotification_200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = createMockResponse(userId);
        given(service.getNotification(eq(notificationId), any())).willReturn(response);

        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").exists());
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - 200 OK + data=null")
    void deleteNotification_200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        doNothing().when(service).deleteNotification(eq(notificationId), any());

        mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/admin/notifications - 200 OK + sentCount 반환")
    void sendAdminNotification_200() throws Exception {
        given(service.sendAdminNotification(any(), any())).willReturn(1);
        String requestBody = """
                {
                    "userId": "%s",
                    "message": "공지사항입니다.",
                    "channel": "EMAIL"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/admin/notifications")
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sentCount").value(1));
    }

    private NotificationResponse createMockResponse(UUID userId) {
        com.ezmeal.notification.domain.entity.Notification notification =
                com.ezmeal.notification.domain.entity.Notification.create(
                        userId, NotificationType.SHIPMENT_STARTED,
                        "배송이 시작되었습니다.", NotificationChannel.EMAIL
                );
        return NotificationResponse.from(notification);
    }
}
