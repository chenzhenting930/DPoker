package com.example.dpoker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameRoomVO {
    private final String type = "RoomInfo";
    /** 房间 ID */
    private Integer roomId;

    /** 房间名称 */
    private String name;

    /** 座位上的玩家（按座位顺序，空位为 null） */
    private List<PlayerVO> players;

    /** 游戏是否已开始 */
    private boolean gameStarted;

    Integer[] blinds;
}
