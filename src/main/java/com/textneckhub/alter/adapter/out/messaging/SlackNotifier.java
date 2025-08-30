// adapter/out/messaging/SlackNotifier.java
package com.textneckhub.alter.adapter.out.messaging;

import com.slack.api.Slack;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.out.NotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
@RequiredArgsConstructor
public class SlackNotifier implements NotifierPort {

    private final Slack slack;

    @Value("${slack.token}")
    private String slackToken;
    @Value("${slack.channel}")
    private String slackChannel;

    @Override
    public Mono<Void> sendSlackAlert(LogMessage msg) {
        String text = String.format(":rotating_light: [%s] %s\n> 서비스: *%s*\n> 메시지: `%s`\n> 시각: %s",
                msg.getLevel(), "알림", msg.getService(), msg.getMessage(), msg.getTimestamp());

        return Mono.fromCallable(() -> {
            ChatPostMessageRequest req = ChatPostMessageRequest.builder()
                    .channel(slackChannel).text(text).build();
            ChatPostMessageResponse resp = slack.methods(slackToken).chatPostMessage(req);
            if (!resp.isOk())
                throw new RuntimeException("Slack API Error: " + resp.getError());
            return resp;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(r -> log.info("Slack 전송 성공 ts={}", r.getTs()))
                .doOnError(e -> log.error("Slack 전송 실패", e))
                .then();
    }
}