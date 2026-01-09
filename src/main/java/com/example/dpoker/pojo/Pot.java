package com.example.dpoker.pojo;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Pot {
    private int amount;
    private Set<Integer> eligiblePlayerIds; // 能赢这个池的玩家

    public Pot(int amount, Set<Integer> eligiblePlayerIds) {
        this.amount = amount;
        this.eligiblePlayerIds = new HashSet<>(eligiblePlayerIds);
    }
}
