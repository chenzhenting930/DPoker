package com.example.dpoker.dto;

import lombok.Data;

@Data
public class ActionRequest {
    private Integer playerId;
    private String userName;
    private String name; // 房间名称
    private String action; // "fold", "call", "raise", "check"
    private Integer amount; // raise 时需要
    private Integer bigBlind = 200; // 新游戏时需要
    private Integer smallBlind = 100; // 新游戏时需要
}
