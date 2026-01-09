package com.example.dpoker.dto;

import lombok.Data;

@Data
public class ActionRequest {
    private Long playerId;
    private String action; // "fold", "call", "raise", "check"
    private Integer amount; // raise 时需要
}
