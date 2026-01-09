package com.example.dpoker.controller;

import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.engine.GameEngine;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GameController {

    @Autowired
    private GameEngine gameEngine;
    @Autowired
    private ObjectMapper objectMapper;

    @MessageMapping("/game/{roomId}/action")
    @SendTo("/topic/game/{roomId}")
    public String handlePlayerAction(
            @DestinationVariable Long roomId,
            @Payload ActionRequest request) throws JsonProcessingException {
//        GameRoom room = gameService.getRoom(roomId);
//        gameEngine.handleExternalAction(room, request.getPlayerId(), request.getAction(), request.getAmount());
        // 注意：实际应验证 playerId 是否合法、是否轮到他等
        System.out.println("roomId = " + roomId+ " request ="+request);
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("content", "服务端收到动作");
        jsonMap.put("roomId", roomId);
        jsonMap.put("playerId", request.getPlayerId());
        return objectMapper.writeValueAsString(jsonMap);
    }


}
