package com.textneckhub.alter.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.textneckhub.alter.domain.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaLogConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SlackNotifier slackNotifier;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "logs", groupId = "log-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received record from Kafka: {}", record.value());

        // WebSocket 토픽으로 실시간 전송
        messagingTemplate.convertAndSend("/topic/logs", record.value());

        // Slack 알림 처리 파이프라인
        Mono.just(record.value())
                .flatMap(this::deserializeLogMessage)
                .filter(logMessage -> "ERROR".equalsIgnoreCase(logMessage.getLevel()))
                .flatMap(slackNotifier::sendSlackAlert)
                .doOnError(e -> log.error("Kafka 메시지 처리 중 오류 발생: {}", e.getMessage()))
                .subscribe();
    }

    private Mono<LogMessage> deserializeLogMessage(String json) {
        try {
            LogMessage message = objectMapper.readValue(json, LogMessage.class);
            return Mono.just(message);
        } catch (JsonProcessingException e) {
            log.error("로그 메시지 역직렬화 실패: {}", json, e);
            return Mono.error(e);
        }
    }
}
