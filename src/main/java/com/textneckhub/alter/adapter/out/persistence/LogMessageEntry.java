package com.textneckhub.alter.adapter.out.persistence;

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

    @Indexed(expireAfterSeconds = 60 * 60 * 24 * 30)
    private Instant ts;

    private String level;
    private String service;
    private String key;
    private String message;
    private String payload;
}