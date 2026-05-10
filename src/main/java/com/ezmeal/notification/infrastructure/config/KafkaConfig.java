package com.ezmeal.notification.infrastructure.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConfig {

    // ── ObjectMapper ──────────────────────────────────────────────────────────
    // Phase 5 리팩토링 후에는 Consumer가 EventEnvelope<T>를 직접 수신하므로
    // 수동 역직렬화 용도는 제거되지만, REST 레이어 등 다른 곳에서 사용할 수 있으므로 유지

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // ── Error Handler ─────────────────────────────────────────────────────────
    // Consumer 예외 발생 시 재시도 정책과 최종 실패 처리를 담당
    // - 복구 가능 예외: ExponentialBackOff로 최대 3회 재시도 (1s → 2s → 4s)
    // - 복구 불가 예외(JsonParseException): 즉시 DLT(*.DLT 토픽)로 전송
    // ※ kafkaListenerContainerFactory는 common의 KafkaConsumerConfig가 제공하므로
    //   해당 factory가 이 Bean을 인식하는지 기동 시 확인 필요

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxAttempts(3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(JsonParseException.class);
        return handler;
    }

    // ── Consumer ──────────────────────────────────────────────────────────────
    // ConsumerFactory는 Spring Boot 자동설정이 notification-service.yml 값으로 생성
    //   spring.kafka.bootstrap-servers, consumer.key/value-deserializer,
    //   consumer.group-id, consumer.auto-offset-reset
    // kafkaListenerContainerFactory는 common의 KafkaConsumerConfig가 제공
    //   (KafkaSecurityInterceptor 포함)
}
