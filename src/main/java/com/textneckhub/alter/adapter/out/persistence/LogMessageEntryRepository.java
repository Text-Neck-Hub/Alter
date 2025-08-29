package com.textneckhub.alter.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface LogMessageEntryRepository extends ReactiveMongoRepository<LogMessageEntry, String> {
    Flux<LogMessageEntry> findTop5ByOrderByTsDesc();
}