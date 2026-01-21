package com.example.dpoker.controller;

import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
import com.example.dpoker.dto.Result;
import com.example.dpoker.service.GameNotificationService;
import com.example.dpoker.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameNotificationService gameNotificationService;

    @MessageMapping("/game/{roomId}/action")
    @SendTo("/topic/game/{roomId}")
    public Result handlePlayerAction(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request)  {

        System.out.println("roomId = " + roomId+ " request ="+request);

        return gameService.onPlayerAction(roomId, request);
    }


    @MessageMapping("/game/{roomId}/ready")
    @SendTo("/topic/game/{roomId}")
    public Result ReadyForGame(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {

        log.info(request.getUserName()+" is ready for game");
        return gameService.startNewGame(roomId,request);
    }

    @MessageMapping("/game/{roomId}/join")
    @SendToUser("/queue")
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

    @MessageMapping("/game/{roomId}/getGameRoomInfo")
    @SendToUser("/queue")
    public Result getGameRoomInfo(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request) {
        return gameService.getGameRoomInfo(roomId,request);
    }


    @MessageMapping("/game/{roomId}/leave")
    @SendToUser("/queue")
    public Result leaveGameRoom(
            @DestinationVariable Integer roomId,
            @Payload ActionRequest request){
        return gameService.leaveGameRoom(roomId,request);
    }





}
