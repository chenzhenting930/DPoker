package com.example.dpoker.engine;

import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.HandRank;
import com.example.dpoker.pojo.Player;
import com.example.dpoker.pojo.Pot;

import java.util.*;
import java.util.stream.Collectors;

public class PotManager {

    // 根据当前玩家下注情况，重建所有底池
    public void rebuildPots(GameRoom room) {
        // 所有在本手牌中投过钱的玩家（包括已弃牌者）
        List<Player> contributors = new ArrayList<>(room.getPlayers().stream()
                .filter(p -> p.getTotalBetInHand() > 0)
                .toList());

        if (contributors.isEmpty()) {
            room.setPots(Collections.emptyList());
            return;
        }

        // 按 totalBetInHand 升序排序
        contributors.sort(Comparator.comparingInt(Player::getTotalBetInHand));

        List<Pot> newPots = new ArrayList<>();
        int previousBet = 0;

        for (int i = 0; i < contributors.size(); i++) {
            Player p = contributors.get(i);
            int currentBet = p.getTotalBetInHand();
            int contribution = currentBet - previousBet;

            if (contribution > 0) {
                // 找出所有下注 >= currentBet 的玩家（能参与此池）
                Set<Integer> eligible = contributors.stream()
                        .filter(player -> player.getTotalBetInHand() >= currentBet)
                        .map(Player::getUserId)
                        .collect(Collectors.toSet());

                int potAmount = contribution * (contributors.size() - i);
                newPots.add(new Pot(potAmount, eligible));
            }

            previousBet = currentBet;
        }

        room.setPots(newPots);
    }

    // 在摊牌时分发所有池
    public void distributeAllPots(GameRoom room, Map<Player, HandRank> rankings) {
        // 所有活跃玩家（用于比牌）
        Set<Player> activePlayers = room.getPlayers().stream()
                .filter(Player::isActive)
                .collect(Collectors.toSet());

        for (Pot pot : room.getPots()) {
            // 找出能竞争此池的活跃玩家
            List<Player> contenders = activePlayers.stream()
                    .filter(p -> pot.getEligiblePlayerIds().contains(p.getUserId()))
                    .collect(Collectors.toList());

            if (contenders.size() == 1) {
                awardPot(contenders.get(0), pot.getAmount());
            } else {
                // 评估这些玩家的手牌
                Map<Player, HandRank> subRankings = new HashMap<>();
                for (Player p : contenders) {
                    subRankings.put(p, rankings.get(p));
                }
                HandRank best = Collections.max(subRankings.values());
                List<Player> winners = subRankings.entrySet().stream()
                        .filter(e -> e.getValue().compareTo(best) == 0)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                int split = pot.getAmount() / winners.size();
                int remainder = pot.getAmount() % winners.size();

                for (int i = 0; i < winners.size(); i++) {
                    int award = split + (i < remainder ? 1 : 0); // 处理余数
                    awardPot(winners.get(i), award);
                }
            }
        }
    }

    private void awardPot(Player player, int amount) {
        player.setChips(player.getChips() + amount);
        System.out.println("💰 玩家 " + player.getUserId() + " 赢得 " + amount + " 筹码");
    }
}
