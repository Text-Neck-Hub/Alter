package com.textneckhub.alter.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.in.HandleLogMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleLogMessageService implements HandleLogMessageUseCase {

    private ObjectMapper objectMapper;

    @Override
    public Mono<LogMessage> handle(String json) {
        LogMessage msg = null;
        try {
            msg = objectMapper.readValue(json, LogMessage.class);
        } catch (JsonProcessingException e) {
            log.error("역직렬화 실패: {}", json, e);
            return Mono.error(e);
        }
        String level = String.valueOf(msg.getLevel());
        boolean needNotify = level.equalsIgnoreCase("INFO")
                || level.equalsIgnoreCase("WARN")
                || level.equalsIgnoreCase("ERROR");

        return needNotify ? Mono.just(msg) : Mono.empty();
    }
}