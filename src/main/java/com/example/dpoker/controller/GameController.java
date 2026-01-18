package com.example.dpoker.controller;

import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
import com.example.dpoker.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @MessageMapping("/game/{roomId}/action")
    @SendTo("/topic/game/{roomId}")
    public GameResponse handlePlayerAction(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request)  {

        System.out.println("roomId = " + roomId+ " request ="+request);

        return gameService.onPlayerAction(roomId, request);
    }

    @MessageMapping("/game/{roomId}/gameStart")
    @SendTo("/topic/game/{roomId}")
    public GameResponse startNewGame(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {
        System.out.println("GAME START -> roomId = " + roomId);

        return gameService.startNewGame(roomId, request.getSmallBlind(),request.getBigBlind());
    }

    @MessageMapping("/game/{roomId}/join")
    @SendTo("/topic/game/{roomId}")
    public GameResponse joinGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {

        return gameService.joinGameRoom(roomId,request);
    }

    @MessageMapping("/game/{roomId}/create")
    public void createGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {

        gameService.createGameRoom(roomId, request);
    }





}
