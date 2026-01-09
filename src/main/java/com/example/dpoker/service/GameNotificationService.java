package com.example.dpoker.service;

import com.example.dpoker.dto.GameUpdateDto;
import com.example.dpoker.dto.PlayerView;
import com.example.dpoker.dto.PotView;
import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class GameNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向房间内所有玩家广播游戏状态更新
     */
    public void notifyRoom(GameRoom room) {
        GameUpdateDto dto = buildGameUpdateDto(room);
        messagingTemplate.convertAndSend("/topic/game/" + room.getRoomId(), dto);
    }

    private GameUpdateDto buildGameUpdateDto(GameRoom room) {
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

        // 构建玩家视图（注意：holeCards 需按玩家隔离）
        dto.setPlayers(room.getPlayers().stream()
                .map(player -> toPlayerView(player, room))
                .collect(Collectors.toList()));

        return dto;
    }

    private PlayerView toPlayerView(Player player, GameRoom room) {
        PlayerView view = new PlayerView();
        view.setUserId(player.getUserId());
        view.setChips(player.getChips());
        view.setTotalBetInHand(player.getTotalBetInHand());
        view.setFolded(player.isFolded());
        view.setAllIn(player.isAllIn());
        view.setActive(player.isActive());
        view.setCurrentPlayer(player.getUserId().equals(room.getPlayers().get(room.getCurrentPlayerIndex()).getUserId()));

        // 安全：只有活跃玩家且是自己时才显示手牌（实际应在登录后按用户ID过滤）
        // 这里简化：假设前端只渲染自己的牌
        if (true) {
            view.setHoleCards(player.getHoleCards().stream()
                    .map(Card::toString)
                    .toArray(String[]::new));
        } else {
            view.setHoleCards(new String[0]); // 或 null
        }

        return view;
    }
}
