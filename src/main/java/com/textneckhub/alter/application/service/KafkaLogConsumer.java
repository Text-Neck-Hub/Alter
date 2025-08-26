package com.textneckhub.alter.application.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class KafkaLogConsumer {
    // @Autowired
    // private SimpMessagingTemplate messagingTemplate;

    // @Autowired
    // private SlackService slackService;

    // @KafkaListener(topics = "logs", groupId = "log-group")
    // public void listen(ConsumerRecord<String, String> record) {
    //     String log = record.value();
        
    //     messagingTemplate.convertAndSend("/topic/logs", log);
       
    //     if (log.contains("ERROR")) {
    //         slackService.sendSlackAlert(log);
    //     }
    // }
}
