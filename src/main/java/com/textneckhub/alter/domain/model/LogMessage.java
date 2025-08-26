package com.textneckhub.alter.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "logs") 
public class LogMessage {

    @Id
    private String id;
    private String message;
    private LocalDateTime timestamp;

    public LogMessage(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}