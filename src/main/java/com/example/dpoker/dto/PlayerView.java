package com.example.dpoker.dto;


import lombok.Data;

@Data
public class PlayerView {
    private Integer userId;
    private String nickname; // 玩家昵称（从 Player.playerName 透传）
    private int chips;
    private int totalBetInHand;
    private boolean folded;
    private boolean allIn;
    private String[] holeCards; // 仅自己可见，他人为空或 null
    private boolean isCurrentPlayer;
    private int index;//座位号
    private String posName;//位置名称 大小盲和庄家
}