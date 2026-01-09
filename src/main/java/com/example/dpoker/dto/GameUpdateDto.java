package com.example.dpoker.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameUpdateDto {
    private String eventType;           // "hand_start", "bet_action", "round_advance", "showdown", etc.
    private Integer roomId;
    private List<PlayerView> players;
    private List<String> communityCards; // e.g., ["Ah", "Kd", "2s"]
    private int currentBet;
    private Long currentPlayerId;
    private List<PotView> pots;
    private String currentRound;        // "PREFLOP", "FLOP", "TURN", "RIVER"
    private Map<String, Object> metadata; // 额外信息，如 winners、folded 玩家等
}
