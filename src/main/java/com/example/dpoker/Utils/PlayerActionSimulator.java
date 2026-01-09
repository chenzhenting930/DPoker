package com.example.dpoker.Utils;

import com.example.dpoker.engine.BettingEngine;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;

import java.util.Random;

public class PlayerActionSimulator {
    private static final Random RAND = new Random();

    public static void simulateAction(GameRoom room, BettingEngine bettingEngine) {
        int currentIndex = room.getCurrentPlayerIndex();
        Player currentPlayer = room.getPlayers().get(currentIndex);
        int currentBet = room.getCurrentBet();
        int myBet = currentPlayer.getBetThisRound();
        int chips = currentPlayer.getChips();

        String action;
        Integer amount = null;

        if (currentBet == 0) {
            // 无人下注：可 check 或 raise
            if (RAND.nextDouble() < 0.7) {
                action = "check";
            } else {
                // 随机加注：2x ~ 5x 大盲（简化）
                int raiseTo = Math.min(chips + myBet, Math.max(20, RAND.nextInt(100) + 20));
                if (raiseTo <= myBet) {
                    action = "check";
                } else {
                    action = "raise";
                    amount = raiseTo;
                }
            }
        } else {
            // 有人下注
            int toCall = currentBet - myBet;

            if (toCall >= chips) {
                // 必须全下或弃牌
                if (RAND.nextDouble() < 0.4) {
                    action = "call"; // 实际是 all-in
                } else {
                    action = "fold";
                }
            } else {
                double r = RAND.nextDouble();
                if (r < 0.2) {
                    action = "fold";
                } else if (r < 0.6) {
                    action = "call";
                } else {
                    // raise: 至少加注到 currentBet * 2
                    int minRaise = currentBet * 2;
                    int maxRaise = chips + myBet;
                    if (minRaise > maxRaise) {
                        action = "call"; // 无法有效加注
                    } else {
                        action = "raise";
                        amount = RAND.nextInt(maxRaise - minRaise + 1) + minRaise;
                    }
                }
            }
        }

        System.out.println("🤖 玩家 " + currentPlayer.getUserId() + " 执行: " + action +
                (amount != null ? " (" + amount + ")" : ""));

        bettingEngine.handleAction(room, currentPlayer.getUserId(), action, amount);
    }

    public static void PlayerBet(GameRoom room, BettingEngine bettingEngine) {

    }
}
