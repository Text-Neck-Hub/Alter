package com.textneckhub.alter.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.textneckhub.alter.domain.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.ZoneId;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogMessageRecorder {

    private final LogMessageEntryRepository repository;
    private final ObjectMapper objectMapper;

    public Mono<LogMessageEntry> save(LogMessage msg, String key) {

        Instant ts = msg.getTimestamp() == null
                ? Instant.now()
                : msg.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(msg))
                .subscribeOn(Schedulers.boundedElastic())
                .map(json -> LogMessageEntry.builder()
                        .ts(ts)
                        .level(safeUpper(msg.getLevel()))
                        .service(safeUpper(msg.getService()))
                        .key(key)
                        .message(msg.getMessage())
                        .payload(json)
                        .build())
                .flatMap(repository::save);
    }

    public Mono<LogMessageEntry> saveRaw(String json, String key) {
        return Mono.fromCallable(() -> parseSafe(json))
                .subscribeOn(Schedulers.boundedElastic())
                .map(parsed -> LogMessageEntry.builder()
                        .ts(Instant.now())
                        .level(parsed.level)
                        .service(parsed.service)
                        .key(key)
                        .message(parsed.message)
                        .payload(json)
                        .build())
                .flatMap(repository::save);
    }

    private static String safeUpper(String s) {
        return s == null ? "UNKNOWN" : s.trim().toUpperCase();
    }

    private static class Parsed {
        String level, service, message;
    }

    private Parsed parseSafe(String json) {
        Parsed p = new Parsed();
        try {
            JsonNode node = objectMapper.readTree(json);
            p.level = safeUpper(text(node, "level"));
            p.service = safeUpper(text(node, "service"));
            p.message = text(node, "message");
        } catch (Exception e) {
            p.level = "UNKNOWN";
            p.service = "UNKNOWN";
            p.message = null;
        }
        return p;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null ? null : v.asText();
    }
}