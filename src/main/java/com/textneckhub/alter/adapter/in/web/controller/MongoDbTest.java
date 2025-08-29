// src/main/java/com/textneckhub/alter/adapter/in/web/controller/MongoDbTest.java
package com.textneckhub.alter.adapter.in.web.controller;

import com.textneckhub.alter.adapter.out.persistence.LogMessageEntry;
import com.textneckhub.alter.adapter.out.persistence.LogMessageEntryRepository;
import com.textneckhub.alter.adapter.out.persistence.LogMessageRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MongoDbTest {

    private final LogMessageRecorder logMessageRecorder;
    private final LogMessageEntryRepository repository;

    @GetMapping("/mongodb/test")
    public Mono<TestResult> testErrorMongoDb() {
        log.info("슬랙 ERROR 채널 테스트 시작");

        String json = """
                {"service":"test-service","level":"ERROR",
                 "message":"이것은 WebFlux를 이용한 리액티브 몽고DB 테스트입니다."}
                """;

        return logMessageRecorder.saveRaw(json, "test-key")
                .doOnSuccess(entry -> log.info("MongoDB 테스트 메시지 저장 완료: {}", entry.getId()))

                .flatMap(saved -> repository.count()
                        .zipWith(repository.findTop5ByOrderByTsDesc().collectList())
                        .map(tuple -> new TestResult(
                                saved.getId(),
                                tuple.getT1(),
                                tuple.getT2())));
    }

    public record TestResult(String savedId, long total, List<LogMessageEntry> recent) {
    }
}