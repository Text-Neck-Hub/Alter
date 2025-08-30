package com.textneckhub.alter;

import com.textneckhub.alter.domain.model.LogMessage;
import reactor.core.publisher.Mono;

public interface NotifierPort {
    Mono<LogMessage> sendSlackAlert(LogMessage msg);
}
