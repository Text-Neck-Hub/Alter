package com.textneckhub.alter.application.service;

import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.repository.LogMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogWriteBackService {

    private static final String LOG_QUEUE_KEY = "log:queue";
    private static final int BATCH_SIZE = 100;

    private final ReactiveRedisTemplate<String, LogMessage> redisTemplate;
    private final LogMessageRepository logMessageRepository;

    /**
     * 로그 메시지를 받아 Redis에 추가하고, 100개가 쌓이면 MongoDB에 저장합니다.
     * @param message 로그 메시지 내용
     * @return 작업 완료 Mono
     */
    public Mono<Void> log(String message) {
        LogMessage logMessage = new LogMessage(message);

        // 1. 로그를 Redis list의 오른쪽에 추가 (RPUSH)
        return redisTemplate.opsForList().rightPush(LOG_QUEUE_KEY, logMessage)
            .flatMap(currentSize -> {
                // 2. 현재 list의 크기가 BATCH_SIZE 이상이면 flush 실행
                if (currentSize >= BATCH_SIZE) {
                    log.info("Log count reached {}, flushing to MongoDB.", currentSize);
                    return flushLogsToMongo();
                }
                return Mono.empty(); // BATCH_SIZE 미만이면 아무것도 하지 않음
            }).then();
    }

    /**
     * Redis에 쌓인 로그를 MongoDB로 옮기는 작업
     * @return 작업 완료 Mono
     */
    private Mono<Void> flushLogsToMongo() {
        // 1. Redis list에서 BATCH_SIZE만큼의 로그를 가져옴 (LPOP을 여러 번)
        Mono<List<LogMessage>> logsToFlush = Flux.range(0, BATCH_SIZE)
            .flatMap(i -> redisTemplate.opsForList().leftPop(LOG_QUEUE_KEY))
            .collectList();

        return logsToFlush
            .flatMapMany(logs -> {
                if (logs.isEmpty()) {
                    return Flux.empty(); // 처리할 로그가 없으면 종료
                }
                log.info("Saving {} logs to MongoDB.", logs.size());
                // 2. 가져온 로그들을 MongoDB에 한꺼번에 저장 (saveAll)
                return logMessageRepository.saveAll(logs);
            })
            .then(); // 모든 작업이 끝나면 Mono<Void> 반환
    }
}
