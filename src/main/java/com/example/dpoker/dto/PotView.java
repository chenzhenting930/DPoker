package com.example.dpoker.dto;

import lombok.Data;

import java.util.Set;

@Data
public class PotView {
    private int amount;
    private Set<Integer> eligiblePlayerIds;
}
