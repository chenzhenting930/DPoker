package com.example.dpoker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dpoker.Mapper.UserMapper;
import com.example.dpoker.Utils.BizThreadPool;
import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameRoomVO;
import com.example.dpoker.dto.Result;
import com.example.dpoker.engine.BettingEngine;
import com.example.dpoker.engine.GameEngine;
import com.example.dpoker.engine.RoundManager;
import com.example.dpoker.entity.User;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import com.example.dpoker.service.event.PlayerActionEvent;
import com.example.dpoker.service.event.RoomEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class GameService {
    private final GameEngine gameEngine;
    private final GameNotificationService notificationService;
    private final RoundManager roundManager;
    private final BettingEngine bettingEngine;
    private final UserMapper userMapper;
    private final Map<Integer, GameRoom> rooms = new ConcurrentHashMap<>();

    public Result onPlayerAction(Integer roomId, ActionRequest action) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在");
        }
        room.enqueue(new PlayerActionEvent(action));
        BizThreadPool.execute(()->processOneEvent(room,action.getPlayerId()));
        if (action.getAction().equals("raise")){
            Player player = room.getPlayerById(action.getPlayerId());
            if (player.getChips() + player.getBetThisRound()<action.getAmount()){
                action.setAmount(player.getChips()+ player.getBetThisRound());
            }
            return Result.success(action.getUserName()+"进行了"+action.getAction()+" 金额："+action.getAmount());
        }
        return Result.success(207,action.getUserName()+"进行了"+action.getAction());
    }

    public Result startNewGame(Integer roomId,ActionRequest actionRequest) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }

        Player player = room.getPlayerById(actionRequest.getPlayerId());
        if (player == null){
            return Result.fail("玩家不存在！");
        }

        int chips = player.getChips();
        if (chips<1000){
            player.setChips(chips+10000);
            player.setPoint(player.getPoint()-10000);
            User user = getUserById(player.getUserId());
            float point = user.getPoint();
            user.setPoint(point-10000);
            userMapper.updateById(user);
        }
        try {
            player.setReady(true);
            if (room.isAllPlayersReady() && room.getPlayers().size()>=3){
                gameEngine.startNewHand(room);
                log.info("游戏开始！");
                return Result.success("游戏开始！",room.toGameRoomVO());
            }
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }


        return Result.success(player.getPlayerName()+"已准备，等待其他玩家准备",room.toGameRoomVO());


    }

    public Result joinGameRoom(Integer roomId ,ActionRequest actionRequest){
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }


        User user = userMapper.selectOne(new LambdaQueryWrapper<>(User.class).eq(User::getId, actionRequest.getPlayerId()));
        if (user == null) {
            return Result.fail("用户不存在！");
        }
        Player player = new Player(actionRequest.getPlayerId(), 10000,actionRequest.getUserName());
        player.setPoint(user.getPoint());
        player.setReady(false);
        if (room.getPlayers().contains(player)){
            return Result.fail(2,"玩家已加入房间！",room.toGameRoomVO());
        } else if (room.isGameStarted()) {
            return Result.fail(3,"游戏已开始，请观战！",room.toGameRoomVO());
        }
        room.getPlayers().add(player);
        //通知房间用户
        notificationService.notifyAllInRoom(room, "玩家"+actionRequest.getUserName()+"加入房间");
        notificationService.notifyAllInRoom(room, room.toGameRoomVO());
        log.info("玩家"+actionRequest.getPlayerId()+"加入房间"+room.getRoomId());
        return Result.success("加入房间成功！",room.toGameRoomVO());
    }

    public Result createGameRoom(Integer roomId , ActionRequest actionRequest){
        if (rooms.containsKey(roomId)){
            return Result.fail("房间已存在！");
        }
        try {
            User user = getUserById(actionRequest.getPlayerId());
            if (user == null) {
                return Result.fail("用户不存在！");
            }
            Player player = new Player(actionRequest.getPlayerId(), 10000,actionRequest.getUserName());
            player.setPoint(user.getPoint());
            player.setReady(false);

            List<Player> playerList = new ArrayList<>();
            playerList.add(player);
            GameRoom room = new GameRoom(roomId, playerList,actionRequest.getName());
            room.setBlinds(new Integer[]{actionRequest.getSmallBlind(), actionRequest.getBigBlind()});
            rooms.put(roomId,room);
            log.info("房间"+roomId+"创建成功");
            return Result.success("房间创建成功",room.toGameRoomVO());
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
        List<GameRoomVO> roomVOS = rooms.values().stream().map(GameRoom::toGameRoomVO).toList();
        return Result.success(roomVOS);
    }

    public Result leaveGameRoom(Integer roomId, ActionRequest request) {
        try{
            GameRoom room = rooms.get(roomId);
            //游戏若已开始，不允许退出房间
            if (room.isGameStarted() && !room.isGameEnded()){
                return Result.fail("游戏已开始，不允许离开房间！");
            }
            User user = getUserById(request.getPlayerId());
            Player player = room.getPlayerById(request.getPlayerId());
            User user1 = settlementToPoint(user, player);
            userMapper.updateById(user1);

            room.removePlayer(request.getPlayerId());
            notificationService.notifyAllInRoom(room,"玩家"+request.getUserName()+"离开房间");
            notificationService.notifyAllInRoom(room,room.toGameRoomVO());
            return Result.success("离开房间成功！",null);
        } catch (Exception e) {
            return Result.fail("离开房间失败！"+e.getMessage());
        }
    }

    public Result getGameRoomInfo(Integer roomId, ActionRequest request) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }
        return Result.success(room.toGameRoomVO());
    }

    public Result getGameUpdate(Integer roomId, ActionRequest request) {
        GameRoom room = rooms.get(roomId);
        if (room == null){
            return Result.fail("房间不存在！请先创建房间！");
        }
        Player player = room.getPlayerById(request.getPlayerId());
        notificationService.notifyRoomToPlayer(room,player);
        return Result.success("成功获取游戏状态！");
    }

    private User getUserById(Integer userId){
        return userMapper.selectOne(new LambdaQueryWrapper<>(User.class).eq(User::getId, userId));
    }

    private User settlementToPoint(User user,Player player){
        float point1 = player.getPoint();
        int chips = player.getChips();
        chips = chips - 10000;
        float point = point1 + chips;
        user.setPoint(point);
        log.info("玩家"+user.getUsername()+"原积分："+point1+" 转换后："+point+",筹码-10000 = "+chips);
        return user;
    }


    public Result globalSettlement(Integer roomId, ActionRequest request) {
        try{
            GameRoom room = rooms.get(roomId);
            if (room == null){
                return Result.fail("房间不存在！");
            }
            if (room.isGameStarted()){
                return Result.fail("游戏已经开始，请结束后进行结算！");
            }

            List<Player> players = room.getPlayers();
            for (Player player : players) {
                User user = getUserById(player.getUserId());
                User user1 = settlementToPoint(user, player);
                userMapper.updateById(user1);
                player.setChips(10000);
                player.setPoint(user1.getPoint());
            }
        } catch (Exception e) {
            log.error("全局结算异常",e);
            return Result.fail("全局结算失败！");
        }
        return Result.success("全局结算完成！");
    }

    public Result getPointRank() {
        List<User> list = userMapper.selectList(
                new QueryWrapper<User>()
                        .select("nickname", "point")      // 1. 先指定查询字段
                        .eq("test", 0)                    // 2. 条件
                        .orderByDesc("point")             // 3. 排序（降序）
        );
        return Result.success("获取排行榜成功",list);
    }
}
