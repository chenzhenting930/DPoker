package com.example.dpoker.dto;


import lombok.Data;

@Data
public class PlayerView {
    private Integer userId;
    private String nickname; // 玩家昵称（从 Player.playerName 透传）
    private int chips;
    private int totalBetInHand;
    /**
     * 本轮已投入底池的筹码（每个下注轮开始时重置为 0）
     * 前端用它计算"跟注额" = currentBet - betThisRound，
     * 这是德州扑克跟注额的正确算法（按本轮算，而非整手累积）。
     */
    private int betThisRound;
    private boolean folded;
    private boolean allIn;
    private String[] holeCards; // 仅自己可见，他人为空或 null
    private boolean isCurrentPlayer;
    private int index;//座位号
    private String posName;//位置名称 大小盲和庄家
    /**
     * 头像标识（"preset:N"），由 GameNotificationService 从 Player.avatar 透传，
     * 前端 PlayerSeat 据此渲染对应 emoji 头像，所有客户端看到同一玩家头像一致。
     */
    private String avatar;
}