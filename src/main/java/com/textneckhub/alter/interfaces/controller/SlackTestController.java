package com.textneckhub.alter.interfaces.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.textneckhub.alter.application.service.SlackNotifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SlackTestController {

    @Autowired
    SlackNotifier slackService;

    @GetMapping("/slack/test")
    public void error(){

        log.info("슬랙 error 채널 테스트");
        System.out.println("슬랙 error 채널 테스트");
        try{slackService.sendMessage("슬랙 테스트");}
        catch(Exception e){e.printStackTrace();}
    }

    
}
