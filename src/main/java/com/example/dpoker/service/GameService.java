package com.example.dpoker.service;

import com.example.dpoker.Utils.BizThreadPool;
import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
import com.example.dpoker.dto.Result;
import com.example.dpoker.engine.BettingEngine;
import com.example.dpoker.engine.GameEngine;
import com.example.dpoker.engine.RoundManager;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import com.example.dpoker.service.event.PlayerActionEvent;
import com.example.dpoker.service.event.RoomEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameEngine gameEngine;
    private final GameNotificationService notificationService;
    private final RoundManager roundManager;
    private final BettingEngine bettingEngine;
    private final Map<Integer, GameRoom> rooms = new ConcurrentHashMap<>();

    public Result onPlayerAction(Integer roomId, ActionRequest action) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在");
        }
        room.enqueue(new PlayerActionEvent(action));
        BizThreadPool.execute(()->processOneEvent(room,action.getPlayerId()));
        return Result.success();
    }

    public Result startNewGame(Integer roomId, int smallBlind, int bigBlind) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }
        gameEngine.startNewHand(room, smallBlind, bigBlind);
        log.info("游戏开始！");
        return Result.success("游戏开始！");
    }

    public Result joinGameRoom(Integer roomId ,ActionRequest actionRequest){
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }

        Player player = new Player(actionRequest.getPlayerId(), 10000);
        if (room.getPlayers().contains(player)){
            return Result.fail("玩家已加入房间！");
        }
        room.getPlayers().add(player);
        //通知房间用户
        notificationService.notifyAllInRoom(room, "玩家"+actionRequest.getPlayerId()+"加入房间");
        return Result.success("加入房间成功！");
    }

    public Result createGameRoom(Integer roomId , ActionRequest actionRequest){
        if (rooms.containsKey(roomId)){
            return Result.fail("房间已存在！");
        }
        try {
            Player player = new Player(actionRequest.getPlayerId(), 10000);
            List<Player> playerList = new ArrayList<>();
            playerList.add(player);
            GameRoom room = new GameRoom(roomId, playerList);
            rooms.put(roomId,room);
            return Result.success("房间创建成功！");
        } catch (Exception e) {
            return Result.fail("房间创建失败！"+e.getMessage());
        }
    }

    public void processOneEvent(GameRoom room,Integer playerId) {
        try {
            RoomEvent event = room.getQueue().take();
            event.handle(room,bettingEngine);

            notificationService.notifyRoom(room);

            if(room.isGameEnded()){
                // 本局游戏结束
                return;
            }

            if (room.isRoundCompleted()){
                roundManager.advanceToNextRound(room);
                notificationService.notifyRoom(room);
            }
        } catch (Exception e) {
            notificationService.notifyPlayer(playerId,e.getMessage());
            log.error("Error handling event", e);
        }
    }

    public Result getGameRoomList() {
        if (rooms.isEmpty()){
            return Result.fail("房间列表为空！");
        }
        return Result.success(rooms);
    }

    public Result leaveGameRoom(Integer roomId, ActionRequest request) {
        try{
            GameRoom room = rooms.get(roomId);
            room.removePlayer(request.getPlayerId());
            notificationService.notifyAllInRoom(room,"玩家"+request.getUserName()+"离开房间");
            return Result.success("离开房间成功！");
        } catch (Exception e) {
            return Result.fail("离开房间失败！"+e.getMessage());
        }
    }
}
