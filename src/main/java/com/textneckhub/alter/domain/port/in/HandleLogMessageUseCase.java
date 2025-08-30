package com.textneckhub.alter.domain.port.in;

import com.textneckhub.alter.domain.model.LogMessage;
import reactor.core.publisher.Mono;

public interface HandleLogMessageUseCase {
    Mono<LogMessage> handle(String json);
}