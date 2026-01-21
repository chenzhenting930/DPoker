package com.example.dpoker.service;

import com.example.dpoker.dto.GameReport;
import com.example.dpoker.dto.GameUpdateDto;
import com.example.dpoker.dto.PlayerView;
import com.example.dpoker.dto.PotView;
import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class GameNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向房间内所有玩家广播游戏状态更新
     */
    public void notifyRoom(GameRoom room) {
//        messagingTemplate.convertAndSend("/topic/game/" + room.getRoomId(), dto);
        for (Player player:room.getPlayers()){
            GameUpdateDto dto = buildGameUpdateDto(room,player);
            messagingTemplate.convertAndSendToUser(player.getUserId().toString(), "/queue", dto);
        }
    }

    public void notifyAllInRoom(GameRoom room,Object message) {
        messagingTemplate.convertAndSend("/topic/game/" + room.getRoomId(), message);
    }

    public void notifyPlayer(Integer playerId,Object message){
        messagingTemplate.convertAndSendToUser(playerId.toString(), "/queue", message);

    }

    private GameUpdateDto buildGameUpdateDto(GameRoom room,Player p) {
        GameUpdateDto dto = new GameUpdateDto();
        dto.setRoomId(room.getRoomId());
        dto.setCommunityCards(room.getCommunityCards().stream()
                .map(Card::toString)
                .collect(Collectors.toList()));
        dto.setCurrentBet(room.getCurrentBet());
        dto.setCurrentRound(room.getCurrentRound());
        dto.setPots(room.getPots().stream()
                .map(pot -> {
                    PotView view = new PotView();
                    view.setAmount(pot.getAmount());
                    view.setEligiblePlayerIds(pot.getEligiblePlayerIds());
                    return view;
                })
                .collect(Collectors.toList()));
        dto.setCurrentPlayerId(room.getCurrentPlayer().getUserId());

        // 构建玩家视图（注意：holeCards 需按玩家隔离）
        dto.setPlayers(
                IntStream.range(0, room.getPlayers().size())          // 0,1,2…
                        .mapToObj(i -> toPlayerView(room.getPlayers().get(i), room, p,i)) // i 就是索引
                        .collect(Collectors.toList())
        );

        dto.setGameEnded(room.isGameEnded());

        dto.setGameReports(GameReport.generateGameReport(room));

        return dto;
    }

    private PlayerView toPlayerView(Player player, GameRoom room,Player p,int index) {
        PlayerView view = new PlayerView();
        view.setUserId(player.getUserId());
        view.setChips(player.getChips());
        view.setTotalBetInHand(player.getTotalBetInHand());
        view.setFolded(player.isFolded());
        view.setAllIn(player.isAllIn());
        view.setCurrentPlayer(player.getUserId().equals(room.getCurrentPlayer().getUserId()));
        view.setIndex(index);
        int buttonIndex = room.getButtonIndex();
        view.setPosName(getPositionName(buttonIndex,index,room.getPlayers().size()));


        if (p.getUserId().equals(player.getUserId())) {
            view.setHoleCards(player.getHoleCards().stream()
                    .map(Card::toString)
                    .toArray(String[]::new));
        } else {
            view.setHoleCards(new String[0]); // 或 null
        }

        return view;
    }

    private String getPositionName(int buttonIndex,int index,int size){
        if (buttonIndex == index){
            return "庄家";
        } else if ((buttonIndex+1)%size == index) {
            return "小盲";
        } else if ((buttonIndex+2)%size == index) {
            return "大盲";
        }
        return "普通位置";
    }
}
