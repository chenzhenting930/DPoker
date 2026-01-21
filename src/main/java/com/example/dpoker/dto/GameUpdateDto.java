package com.example.dpoker.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameUpdateDto {
    private final String type = "GameUpdate";
    private Integer roomId;
    private List<PlayerView> players;
    private List<String> communityCards; // e.g., ["Ah", "Kd", "2s"]
    private int currentBet;
    private Integer currentPlayerId;
    private List<PotView> pots;
    private String currentRound;        // "PREFLOP", "FLOP", "TURN", "RIVER"
    private boolean gameEnded;
    private List<GameReport> gameReports;
}
