package com.textneckhub.alter.domain.repository;

import com.textneckhub.alter.domain.model.LogMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogMessageRepository extends ReactiveMongoRepository<LogMessage, String> {
}
