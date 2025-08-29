package com.textneckhub.alter.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.out.NotifierPort;
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
    private final NotifierPort notifierPort;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.log-topic:log-message}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received record: key={} value={}", record.key(), record.value());
        messagingTemplate.convertAndSend("/topic/logs", record.value());
        Mono.just(record.value())
                .flatMap(this::deserializeLogMessage)
                .filter(logMessage -> "INFO".equalsIgnoreCase(logMessage.getLevel()))
                .flatMap(notifierPort::sendSlackAlert)
                .doOnError(e -> log.error("Kafka 처리 오류: {}", e.getMessage()))
                .subscribe();
    }

    private Mono<LogMessage> deserializeLogMessage(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, LogMessage.class));
        } catch (JsonProcessingException e) {
            log.error("역직렬화 실패: {}", json, e);
            return Mono.error(e);
        }
    }
}
