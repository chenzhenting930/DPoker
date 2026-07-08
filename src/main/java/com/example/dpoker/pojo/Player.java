package com.example.dpoker.pojo;

import com.example.dpoker.dto.PlayerVO;
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
    private String playerName;
    private boolean ready; // 是否准备
    private float point; // 用户积分
    /**
     * 当前筹码
     */
    private int chips;
    private boolean folded = false;     // 是否弃牌
    private List<Card> holeCards = new ArrayList<>(); // 底牌（2张）
    private int betThisRound = 0; // 本轮已投入底池的筹码
    private boolean AllIn = false;
    private int totalBetInHand = 0; // 整手牌累计投入底池的筹码
    /**
     * 头像标识（来自 User.avatar，加入房间时透传）。
     * 格式 "preset:N"，由前端 UserAvatar 渲染为对应 emoji 头像。
     * 跟随玩家在整个牌局内一致，所有客户端都能看到同一头像。
     */
    private String avatar;

    public void resetBetThisRound() {
        this.betThisRound = 0;
    }
    public void addToTotalBet(int amount) {
        this.totalBetInHand += amount;
    }

    public boolean hasMetCurrentBet(int currentBet) {
        return betThisRound >= currentBet || chips == 0; // 全下也算满足
    }

    public Player(int userId, int chips,String playerName) {
        this.userId = userId;
        this.chips = chips;
        this.playerName = playerName;
    }

    /**
     * 是否存活（是否弃牌）
     */
    public boolean isActive() {
        return !folded;
    }

    public PlayerVO toPlayerVO(){
        return PlayerVO.builder()
                .userId(userId)
                .playerName(playerName)
                .point(point)
                .chips(chips)
                .ready(ready)
                .avatar(avatar)
                .build();
    }
}
