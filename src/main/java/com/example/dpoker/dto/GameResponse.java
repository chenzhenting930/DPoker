package com.example.dpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResponse {
    private String content;
    private Integer roomId;
    private Integer playerId;
    private String action;
    private int state=1;//1成功0失败

}
