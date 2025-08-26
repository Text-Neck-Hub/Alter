package com.textneckhub.alter.interfaces.controller;

import com.textneckhub.alter.application.service.SlackNotifier;
import com.textneckhub.alter.domain.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SlackTestController {

    private final SlackNotifier slackNotifier;

    @GetMapping("/slack/test")
    public Mono<Void> testErrorSlack() {
        log.info("슬랙 ERROR 채널 테스트 시작");

        LogMessage testLogMessage = new LogMessage(
                "test-service",
                "ERROR",
                "이것은 WebFlux를 이용한 리액티브 슬랙 알림 테스트입니다.");

        return slackNotifier.sendSlackAlert(testLogMessage)
                .doOnSuccess(v -> log.info("슬랙 테스트 메시지 전송 요청 완료."));
    }
}
