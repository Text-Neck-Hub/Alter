package com.textneckhub.alter.adapter.out.persistence;

import com.textneckhub.alter.domain.model.LogMessage;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "logs")
public class LogMessageEntry {

    @Id
    private String id;

    @Indexed(expireAfter = "30d")
    private Instant ts;

    private String level;
    private String service;
    private String key;
    private String message;
    private String payload;

    public LogMessage toDomain() {
        return new LogMessage(
                this.service,
                this.level,
                this.message);
    }

}