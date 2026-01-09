package com.example.dpoker.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "userId")
public class Player {
    private Integer userId;
    /**
     * 当前筹码
     */
    private int chips;
    private boolean folded = false;     // 是否弃牌
    private List<Card> holeCards = new ArrayList<>(); // 底牌（2张）
    private int betThisRound = 0; // 本轮已投入底池的筹码
    private boolean AllIn = false;
    private int totalBetInHand = 0; // 整手牌累计投入底池的筹码

    public void resetBetThisRound() {
        this.betThisRound = 0;
    }
    public void addToTotalBet(int amount) {
        this.totalBetInHand += amount;
    }
    public void resetTotalBet() {
        this.totalBetInHand = 0;
    }

    public boolean hasMetCurrentBet(int currentBet) {
        return betThisRound >= currentBet || chips == 0; // 全下也算满足
    }

    public Player(int userId, int chips) {
        this.userId = userId;
        this.chips = chips;
    }

    /**
     *   工具方法：是否存活（是否弃牌）
     */
    public boolean isActive() {
        return !folded;
    }
}
