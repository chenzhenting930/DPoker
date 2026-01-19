package com.example.dpoker.controller;

import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
import com.example.dpoker.dto.Result;
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
    public Result handlePlayerAction(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request)  {

        System.out.println("roomId = " + roomId+ " request ="+request);

        return gameService.onPlayerAction(roomId, request);
    }

    @MessageMapping("/game/{roomId}/gameStart")
    @SendTo("/topic/game/{roomId}")
    public Result startNewGame(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {
        System.out.println("GAME START -> roomId = " + roomId);

        return gameService.startNewGame(roomId, request.getSmallBlind(),request.getBigBlind());
    }

    @MessageMapping("/game/{roomId}/join")
    @SendTo("/queue")
    public Result joinGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {

        return gameService.joinGameRoom(roomId,request);
    }

    @MessageMapping("/game/{roomId}/create")
    @SendToUser("/queue")
    public Result createGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {

        return gameService.createGameRoom(roomId, request);
    }

    @MessageMapping("/getGameRoomList")
    @SendToUser("/queue")
    public Result getGameRoomList() {
        return gameService.getGameRoomList();
    }

    @MessageMapping("/game/{roomId}/leave")
    @SendToUser("/queue")
    public Result leaveGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request){
        return gameService.leaveGameRoom(roomId,request);
    }





}
