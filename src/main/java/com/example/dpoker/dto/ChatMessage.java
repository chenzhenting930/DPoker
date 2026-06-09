package com.example.dpoker.dto;

import lombok.Data;

/**
 * 聊天消息 DTO
 * ---------------------------------------------------------------------------
 * 用于房间内玩家间的文字聊天。
 * 前端通过 /app/game/{roomId}/chat 发送，
 * 后端通过 /topic/game/{roomId} 广播给房间内所有玩家。
 */
@Data
public class ChatMessage {
    /** 消息类型标识，前端据此区分聊天消息与游戏状态更新 */
    private String type = "chat";
    /** 发送者 userId */
    private Integer playerId;
    /** 发送者昵称 */
    private String userName;
    /** 聊天文本内容 */
    private String text;
}