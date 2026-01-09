package com.example.dpoker.engine;

import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoundManager{
    private final BettingEngine bettingEngine;

    public void advanceToNextRound(GameRoom room) {
        new PotManager().rebuildPots(room);
        // 1. 重置本轮下注状态
        for (Player p : room.getPlayers()) {
            p.resetBetThisRound();
        }
        room.setCurrentBet(0);
        room.setRoundCompleted(false);

        // 2. 推进轮次
        if(room.getPlayers().stream().filter(Player::isActive).toList().size() == 1){
            room.setRound(4);
        }else {
            room.setRound(room.getRound() + 1);
        }


        // 3. 根据轮次发公共牌 或 摊牌
        switch (room.getRound()) {
            case 1 -> // Flop
                    dealCommunityCards(room, 3);
            // Turn
            case 2, 3 -> // River
                    dealCommunityCards(room, 1);
            case 4 -> { // Showdown
                new ShowdownEngine().distributePot(room);
                room.setGameEnded(true);
                return;
            }
            default -> throw new IllegalStateException("Invalid round: " + room.getRound());
        }
        System.out.println("公共牌 = " + room.getCommunityCards());
        // 4. 初始化新下注轮（从第一个活跃玩家开始）
        initializeBettingRound(room);
    }

    private void initializeBettingRound(GameRoom room) {
        List<Integer> order = new ArrayList<>();
        int buttonIndex = room.getButtonIndex();
        // 简化：从 index=0 开始找第一个活跃玩家
        int n = room.getPlayers().size();
        for (int i = 0; i < n; i++) {
            int index = (buttonIndex + 1 + i) % n;
            if (room.getPlayers().get(index).isActive()) { //从庄家的下一位开始
                order.add(index);
            }
        }
        room.setBettingOrder(order);
        room.setCurrentPlayerIndex(order.isEmpty() ? -1 : order.get(0));
    }

    private void dealCommunityCards(GameRoom room, int count) {
        room.getDeck().remove(room.getDeck().size() - 1); //切一张牌
        for (int i = 0; i < count; i++) {
            Card card = room.getDeck().remove(room.getDeck().size() - 1);
            room.getCommunityCards().add(card);
        }
    }


}
