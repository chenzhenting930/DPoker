package com.example.dpoker.engine;

import com.example.dpoker.Utils.HandEvaluator;
import com.example.dpoker.pojo.*;

import java.util.*;


public class ShowdownEngine{
    public void distributePot(GameRoom room) {
        List<Player> activePlayers = room.getPlayers().stream()
                .filter(Player::isActive)
                .toList();

        if (activePlayers.size() == 1) {
            // 所有池归他
            int total = room.getTotalPotsAmount();
            awardPot(activePlayers.get(0), total);
        } else {
            Map<Player, HandRank> rankings = new HashMap<>();
            for (Player p : activePlayers) {
                List<Card> allCards = new ArrayList<>(p.getHoleCards());
                allCards.addAll(room.getCommunityCards());
                HandRank rank = HandEvaluator.evaluateBestHand(allCards);
                rankings.put(p, rank);
                System.out.println("玩家 " + p.getUserId() + " 手牌: " + rank.getType() + ", 关键牌: " + Arrays.toString(rank.getKeyRanks()));
            }
            new PotManager().distributeAllPots(room,rankings);
//            // 找出最高排名
//            HandRank best = Collections.max(rankings.values());
//            List<Player> winners = rankings.entrySet().stream()
//                    .filter(e -> e.getValue().compareTo(best) == 0)
//                    .map(Map.Entry::getKey)
//                    .toList();
//
//            int splitPot = room.getPot() / winners.size();
//            for (Player w : winners) {
//                awardPot(w, splitPot);
//            }
        }
    }

    private void awardPot(Player player, int amount) {
        player.setChips(player.getChips() + amount);
        System.out.println("🎉 玩家 " + player.getUserId() + " 赢得底池: " + amount);
    }
}
