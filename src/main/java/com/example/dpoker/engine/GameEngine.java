package com.example.dpoker.engine;

import com.example.dpoker.Utils.DeckUtils;
import com.example.dpoker.Utils.PlayerActionSimulator;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import com.example.dpoker.service.GameNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@RequiredArgsConstructor
public class GameEngine {
    private final BettingEngine bettingEngine;
    private final RoundManager roundManager;
    private final GameNotificationService notificationService;
    /**
     * 开始一局新游戏（发底牌 + 收盲注）
     * @param smallBlind 小盲注筹码数
     * @param bigBlind 大盲注筹码数
      */
    public void startNewHand(GameRoom room, int smallBlind, int bigBlind) {
        room.setGameStarted(true);
        room.setGameEnded(false);
        // 1. 重置玩家状态（保留 chips，清空手牌和弃牌状态）
        for (Player p : room.getPlayers()) {
            p.setFolded(false);
            p.getHoleCards().clear();
            p.setAllIn(false);
            p.setBetThisRound(0);
            p.setTotalBetInHand(0);
            p.setReady(false);
        }

        // 2. 洗一副新牌
        room.resetGameRoomForANewGame();

        // 3. 发底牌（每人2张）
        dealHoleCards(room);

        // 4. 收取盲注（假设小盲在 index=0，大盲在 index=1）
        collectBlinds(room, smallBlind, bigBlind);

        // 5. 初始化轮次
        room.setRound(0); // preflop
        room.setGameEnded(false);

        new PotManager().rebuildPots(room);

        initializePreflopBettingOrder(room);

        notificationService.notifyRoom(room);
    }

    private void dealHoleCards(GameRoom room) {
        //发底牌，每人一张，发两轮
        List<Player> players = room.getPlayers();
        int round = 2;
        while (round > 0){
            for (Player player : players) {
                DeckUtils.dealCard(player,room.getDeck());
            }
            round--;
        }
    }

    private void collectBlinds(GameRoom room, int smallBlind, int bigBlind) {
        List<Player> players = room.getPlayers();
        if (players.size() < 3) {
            throw new IllegalArgumentException("至少需要3名玩家");
        }

        // 简化：固定小盲=0号位，大盲=1号位（实际应动态计算按钮位置）
        int buttonIndex = room.getButtonIndex();
        int smallBlindIndex = (buttonIndex + 1) % players.size();
        int bigBlindIndex = (buttonIndex + 2) % players.size();
        Player smallBlindPlayer = players.get(smallBlindIndex);
        Player bigBlindPlayer = players.get(bigBlindIndex);

        // 扣除筹码
        smallBlindPlayer.setChips(smallBlindPlayer.getChips() - smallBlind);
        smallBlindPlayer.setBetThisRound(smallBlind);
        smallBlindPlayer.addToTotalBet(smallBlind);

        bigBlindPlayer.setChips(bigBlindPlayer.getChips() - bigBlind);
        bigBlindPlayer.setBetThisRound(bigBlind);
        bigBlindPlayer.addToTotalBet(bigBlind);


        // 记录已下注（preflop 轮）
        // 注意：这里简化处理，真实场景需记录 per-player bet

        room.setCurrentBet(bigBlind); // 当前最高下注是大盲
    }

    private void initializePreflopBettingOrder(GameRoom room) {
        // 假设小盲=0, 大盲=1 → 从 index=2 开始
        //从大盲的下一位开始，一直到大盲结束，注意，这是收取盲注以后的下注过程
        List<Integer> order = new ArrayList<>();
        int buttonIndex = room.getButtonIndex();
        int n = room.getPlayers().size();
        for (int i = 0; i < n; i++) {
            //buttonIndex + i + 3 表示从庄家后的第三位开始， 即UTG，大盲的后一位
            int index = (buttonIndex + i + 3) % n;
            if (room.getPlayers().get(index).isActive()) order.add(index);
        }

        room.setBettingOrder(order);
        room.setCurrentPlayerIndex(order.get(0));
    }

    // 新增：驱动整个游戏直到结束,模拟用
    public void runCompleteHand(GameRoom room, int smallBlind, int bigBlind) {
        startNewHand(room, smallBlind, bigBlind);
        notificationService.notifyRoom(room);

        while (!room.isGameEnded()) {
            if (room.isRoundCompleted()) {
                roundManager.advanceToNextRound(room);

                notificationService.notifyRoom(room);
            } else {
//                // 模拟自动下注（教学用）：全部 check 或 call
//                simulateAutoActions(room);
                PlayerActionSimulator.simulateAction(room,bettingEngine);

                notificationService.notifyRoom(room);
            }
        }
        notificationService.notifyRoom(room);
    }

    // 教学用：自动完成所有下注（避免手动输入）
    private void simulateAutoActions(GameRoom room) {
        while (!room.isRoundCompleted() && !room.isGameEnded()) {
            int currentIndex = room.getCurrentPlayerIndex();
            Player currentPlayer = room.getPlayers().get(currentIndex);

            // 简单策略：如果 currentBet == 0 → check；否则 call
            if (room.getCurrentBet() == currentPlayer.getBetThisRound()) {
                bettingEngine.handleAction(room, currentPlayer.getUserId(), "check", null);
            } else {
                bettingEngine.handleAction(room, currentPlayer.getUserId(), "call", null);
            }
        }
    }
}
