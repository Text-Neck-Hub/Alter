package com.textneckhub.alter.application.service;

import com.slack.api.Slack;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.textneckhub.alter.domain.model.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class SlackNotifier {

    private final Slack slack;
    private final String slackToken;
    private final String slackChannel;

    public SlackNotifier(
            Slack slack,
            @Value("${slack.token}") String slackToken,
            @Value("${slack.channel}") String slackChannel
    ) {
        this.slack = slack;
        this.slackToken = slackToken;
        this.slackChannel = slackChannel;
    }

    public Mono<Void> sendSlackAlert(LogMessage logMessage) {
        String message = formatAlertMessage(logMessage);
        return sendMessage(message);
    }

    private Mono<Void> sendMessage(String message) {
        return Mono.fromCallable(() -> {
                    ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                            .channel(slackChannel)
                            .text(message)
                            .build();

                    ChatPostMessageResponse response = slack.methods(slackToken).chatPostMessage(request);

                    if (!response.isOk()) {
                        throw new RuntimeException("Slack API Error: " + response.getError());
                    }
                    log.info("Slack message sent successfully.");
                    return response;
                })
                .subscribeOn(Schedulers.boundedElastic()) // I/O-intensive task on a dedicated thread pool
                .doOnError(e -> log.error("Failed to send Slack message", e))
                .then();
    }

    private String formatAlertMessage(LogMessage logMessage) {
        return String.format(
                ":rotating_light: [%s] %s 알림 :rotating_light:\n> 서비스: *%s*\n> 메시지: `%s`\n> 발생시각: %s",
                logMessage.level().toUpperCase(),
                logMessage.level(),
                logMessage.service(),
                logMessage.message(),
                logMessage.timestamp()
        );
    }
}
