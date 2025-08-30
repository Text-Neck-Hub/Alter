package com.textneckhub.alter.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.textneckhub.alter.adapter.out.messaging.SlackNotifier;
import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.in.HandleLogMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaLogConsumer {
    private final SlackNotifier notifierPort;
    private final HandleLogMessageUseCase handleLogMessage;

    @KafkaListener(topics = "${app.kafka.log-topic:log-message}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received record: key={} value={}", record.key(), record.value());

        Mono.just(record.value())
                .flatMap(handleLogMessage::handle)
                .flatMap(notifierPort::sendSlackAlert)
                .doOnError(e -> log.error("Kafka 처리 오류: {}", e.getMessage(), e))
                .subscribe();
    }

}