package com.textneckhub.alter.domain.port.in;

import com.textneckhub.alter.domain.model.LogMessage;
import reactor.core.publisher.Mono;

public interface HandleLogMessage {
    Mono<LogMessage> handle(LogMessage msg);
}