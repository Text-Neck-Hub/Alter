package com.textneckhub.alter.application.usecase;

import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.in.HandleLogMessage;
import com.textneckhub.alter.domain.port.out.NotifierPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogMessageUseCase implements HandleLogMessage {

    private final NotifierPort notifierPort;

    @Override
    public Mono<LogMessage> handle(LogMessage msg) {

        String level = String.valueOf(msg.getLevel());
        boolean needNotify = level.equalsIgnoreCase("INFO")
                || level.equalsIgnoreCase("WARN")
                || level.equalsIgnoreCase("ERROR");

        return needNotify ? notifierPort.sendSlackAlert(msg) : Mono.empty();
    }
}