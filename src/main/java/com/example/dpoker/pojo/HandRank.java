package com.example.dpoker.pojo;

import lombok.Value;

@Value
public class HandRank implements Comparable<HandRank> {
    Type type;
    int[] keyRanks; // 用于比较的关键点数（按重要性降序）

    public enum Type {
        HIGH_CARD(1),
        ONE_PAIR(2),
        TWO_PAIR(3),
        THREE_OF_A_KIND(4),
        STRAIGHT(5),
        FLUSH(6),
        FULL_HOUSE(7),
        FOUR_OF_A_KIND(8),
        STRAIGHT_FLUSH(9),
        ROYAL_FLUSH(10);

        private final int rank;
        Type(int rank) { this.rank = rank; }
        public int getRank() { return rank; }
    }

    @Override
    public int compareTo(HandRank other) {
        // 先比牌型等级
        int typeCompare = Integer.compare(this.type.getRank(), other.type.getRank());
        if (typeCompare != 0) return typeCompare;

        // 牌型相同时，逐个比较 keyRanks
        for (int i = 0; i < this.keyRanks.length; i++) {
            int cmp = Integer.compare(this.keyRanks[i], other.keyRanks[i]);
            if (cmp != 0) return cmp;
        }
        return 0; // 完全相同（平分彩池）
    }

    // 工厂方法：简化创建
    public static HandRank of(Type type, int... keyRanks) {
        return new HandRank(type, keyRanks);
    }
}
