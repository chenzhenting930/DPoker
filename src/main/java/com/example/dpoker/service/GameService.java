package com.example.dpoker.service;

import com.example.dpoker.Utils.BizThreadPool;
import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
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

    public GameResponse onPlayerAction(Integer roomId, ActionRequest action) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return GameResponse.builder().content("房间不存在！请先创建房间！").build();
        }
        room.enqueue(new PlayerActionEvent(action));
        BizThreadPool.execute(()->processOneEvent(room,action.getPlayerId()));
        return GameResponse.builder().content("服务端收到操作："+action).action(action.getAction()).playerId(action.getPlayerId()).build();
    }

    public GameResponse startNewGame(Integer roomId, int smallBlind, int bigBlind) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return GameResponse.builder().content("房间不存在！").build();
        }
        gameEngine.startNewHand(room, smallBlind, bigBlind);
        log.info("游戏开始！");
        return GameResponse.builder().content("游戏开始！").build();
    }

    public GameResponse joinGameRoom(Integer roomId ,ActionRequest actionRequest){
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return GameResponse.builder().content("房间不存在！").build();
        }

        Player player = new Player(actionRequest.getPlayerId(), 10000);
        if (room.getPlayers().contains(player)){
            return GameResponse.builder().content("玩家"+player.getUserId()+"已经在房间里了！").build();
        }
        room.getPlayers().add(player);
        return GameResponse.builder().content("玩家"+player.getUserId()+"加入房间成功").state(1).build();
    }

    public void createGameRoom(Integer roomId ,ActionRequest actionRequest){
        if (rooms.containsKey(roomId)){
            notificationService.notifyPlayer(actionRequest.getPlayerId(),"房间已存在！");
            return;
        }
        try {
            Player player = new Player(actionRequest.getPlayerId(), 10000);
            List<Player> playerList = new ArrayList<>();
            playerList.add(player);
            GameRoom room = new GameRoom(roomId, playerList);
            rooms.put(roomId,room);
            notificationService.notifyPlayer(actionRequest.getPlayerId(),"房间创建成功！");
        } catch (Exception e) {
            notificationService.notifyPlayer(actionRequest.getPlayerId(),"房间创建异常！error msg:"+e.getMessage());
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

}
