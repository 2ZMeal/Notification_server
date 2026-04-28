package com.ezmeal.notification.infrastructure.channel;

import com.ezmeal.notification.domain.entity.Notification;
import com.ezmeal.notification.domain.entity.NotificationType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannelSender implements ChannelSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    private static final Map<NotificationType, String> SUBJECT_MAP = Map.ofEntries(
            Map.entry(NotificationType.ORDER_STATUS_CHANGED,  "[EzMeal] 주문 상태가 변경되었습니다"),
            Map.entry(NotificationType.PAYMENT_SUCCESS,       "[EzMeal] 결제가 완료되었습니다"),
            Map.entry(NotificationType.PAYMENT_CANCELLED,     "[EzMeal] 결제가 취소되었습니다"),
            Map.entry(NotificationType.SHIPMENT_STARTED,      "[EzMeal] 배송이 시작되었습니다"),
            Map.entry(NotificationType.SHIPMENT_DELIVERED,    "[EzMeal] 배송이 완료되었습니다"),
            Map.entry(NotificationType.ADMIN_BROADCAST,       "[EzMeal] 공지사항")
    );

    @Override
    public void send(Notification notification, String recipientEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipientEmail);
            helper.setSubject(SUBJECT_MAP.getOrDefault(notification.getType(), "[EzMeal] 알림"));
            helper.setText(buildHtmlBody(notification), true);
            mailSender.send(mimeMessage);
            notification.updateSentAt(LocalDateTime.now());
            log.info("[EMAIL-SENT] to={}, type={}", recipientEmail, notification.getType());
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }

    private String buildHtmlBody(Notification notification) {
        String title = SUBJECT_MAP.getOrDefault(notification.getType(), "EzMeal 알림")
                .replace("[EzMeal] ", "");
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f5f5f5;font-family:'Apple SD Gothic Neo',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5;padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                        <!-- 헤더 -->
                        <tr>
                          <td style="background-color:#FF6B35;padding:28px 40px;">
                            <span style="font-size:22px;font-weight:bold;color:#ffffff;letter-spacing:-0.5px;">🍽 EzMeal</span>
                          </td>
                        </tr>

                        <!-- 제목 -->
                        <tr>
                          <td style="padding:36px 40px 16px;">
                            <h2 style="margin:0;font-size:20px;font-weight:700;color:#1a1a1a;line-height:1.4;">%s</h2>
                          </td>
                        </tr>

                        <!-- 본문 -->
                        <tr>
                          <td style="padding:0 40px 32px;">
                            <p style="margin:0 0 16px;font-size:15px;color:#333333;line-height:1.7;">안녕하세요, EzMeal입니다.</p>
                            <div style="background-color:#fef6f2;border-left:4px solid #FF6B35;border-radius:4px;padding:16px 20px;margin:20px 0;">
                              <p style="margin:0;font-size:15px;color:#333333;line-height:1.7;">%s</p>
                            </div>
                            <p style="margin:16px 0 0;font-size:14px;color:#666666;line-height:1.7;">
                              궁금하신 점이 있으시면 고객센터로 문의해 주세요.
                            </p>
                          </td>
                        </tr>

                        <!-- 구분선 -->
                        <tr>
                          <td style="padding:0 40px;">
                            <hr style="border:none;border-top:1px solid #eeeeee;margin:0;">
                          </td>
                        </tr>

                        <!-- 푸터 -->
                        <tr>
                          <td style="padding:24px 40px;background-color:#fafafa;">
                            <p style="margin:0 0 4px;font-size:12px;color:#999999;">ⓒ 2026 EzMeal. All rights reserved.</p>
                            <p style="margin:0;font-size:12px;color:#bbbbbb;">본 메일은 발신 전용으로 회신이 불가합니다.</p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, notification.getMessage());
    }
}
