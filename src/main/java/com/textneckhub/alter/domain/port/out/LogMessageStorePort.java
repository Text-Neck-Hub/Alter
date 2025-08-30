package com.textneckhub.alter.domain.port.out;

import com.textneckhub.alter.domain.model.LogMessage;
import reactor.core.publisher.Mono;

public interface LogMessageStorePort {
        Mono<LogMessage> save(LogMessage msg, String key);

        Mono<LogMessage> saveRaw(String json, String key);
}
