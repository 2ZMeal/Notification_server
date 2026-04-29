package com.ezmeal.notification.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    // ── ObjectMapper ──────────────────────────────────────────────────────────
    // 7개 Kafka Consumer가 수동 JSON 역직렬화(String → Payload)에 사용

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // ── Consumer ──────────────────────────────────────────────────────────────
    // ConsumerFactory는 Spring Boot 자동설정이 notification-service.yml 값으로 생성
    //   spring.kafka.bootstrap-servers, consumer.key/value-deserializer,
    //   consumer.group-id, consumer.auto-offset-reset
    // kafkaListenerContainerFactory는 common의 KafkaConsumerConfig가 제공
    //   (KafkaSecurityInterceptor 포함)
}
