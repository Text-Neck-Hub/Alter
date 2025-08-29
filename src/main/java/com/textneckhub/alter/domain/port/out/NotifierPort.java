package com.textneckhub.alter.domain.port.out;

import com.textneckhub.alter.domain.model.LogMessage;
import reactor.core.publisher.Mono;

public interface NotifierPort {
    Mono<Void> sendSlackAlert(LogMessage logMessage);
}
