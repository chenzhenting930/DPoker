package com.example.dpoker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerVO {
    private Integer userId;
    private String playerName;

    private float point; // 用户积分
    private int chips; // 当前筹码,游戏内
    private boolean ready; // 是否准备
    /**
     * 头像标识（"preset:N"），由 Player.avatar 透传，
     * 前端 UserAvatar 据此渲染 emoji 头像。
     */
    private String avatar;
}
