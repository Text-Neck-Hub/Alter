package com.textneckhub.alter.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LogMessageEntryRepository extends ReactiveMongoRepository<LogMessageEntry, String> {
}