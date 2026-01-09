package com.example.dpoker.Utils;

import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.HandRank;

import java.util.*;
import java.util.stream.Collectors;

public class HandEvaluator {

    // 从7张牌中找出最佳5张组合的 HandRank
    public static HandRank evaluateBestHand(List<Card> sevenCards) {
        if (sevenCards.size() < 5) {
            throw new IllegalArgumentException("At least 5 cards required");
        }

        List<List<Card>> all5CardHands = generateCombinations(sevenCards, 5);
        return all5CardHands.stream()
                .map(HandEvaluator::evaluate5CardHand)
                .max(HandRank::compareTo)
                .orElseThrow();
    }

    // 生成 C(n, k) 组合
    private static <T> List<List<T>> generateCombinations(List<T> elements, int k) {
        List<List<T>> result = new ArrayList<>();
        combine(elements, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static <T> void combine(List<T> elements, int k, int start, List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < elements.size(); i++) {
            current.add(elements.get(i));
            combine(elements, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // 评估固定的5张牌
    private static HandRank evaluate5CardHand(List<Card> fiveCards) {
        // 1. 按点数分组
        Map<Integer, List<Card>> rankGroups = fiveCards.stream()
                .collect(Collectors.groupingBy(card -> card.getRank().getValue()));

        // 2. 按花色分组
        Map<Card.Suit, List<Card>> suitGroups = fiveCards.stream()
                .collect(Collectors.groupingBy(Card::getSuit));

        boolean isFlush = suitGroups.values().stream().anyMatch(list -> list.size() == 5);
        boolean isStraight = isStraight(fiveCards);

        if (isStraight && isFlush) {
            int high = getHighestStraightCard(fiveCards);
            if (high == 14) { // A-K-Q-J-10
                return HandRank.of(HandRank.Type.ROYAL_FLUSH);
            } else {
                return HandRank.of(HandRank.Type.STRAIGHT_FLUSH, high);
            }
        }

        List<Integer> ranksDesc = rankGroups.keySet().stream()
                .sorted(Collections.reverseOrder())
                .toList();

        int[] counts = rankGroups.values().stream()
                .mapToInt(List::size)
                .toArray();
        Arrays.sort(counts);

        if (Arrays.equals(counts, new int[]{1, 4})) {
            // 四条
            int fourRank = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 4).findFirst().get();
            int kicker = ranksDesc.stream().filter(r -> r != fourRank).findFirst().get();
            return HandRank.of(HandRank.Type.FOUR_OF_A_KIND, fourRank, kicker);
        }

        if (Arrays.equals(counts, new int[]{2, 3})) {
            // 葫芦
            int threeRank = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 3).findFirst().get();
            int pairRank = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 2).findFirst().get();
            return HandRank.of(HandRank.Type.FULL_HOUSE, threeRank, pairRank);
        }

        if (isFlush) {
            return HandRank.of(HandRank.Type.FLUSH, ranksDesc.get(0), ranksDesc.get(1), ranksDesc.get(2), ranksDesc.get(3), ranksDesc.get(4));
        }

        if (isStraight) {
            return HandRank.of(HandRank.Type.STRAIGHT, getHighestStraightCard(fiveCards));
        }

        if (Arrays.equals(counts, new int[]{1, 1, 3})) {
            int threeRank = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 3).findFirst().get();
            List<Integer> kickers = ranksDesc.stream().filter(r -> r != threeRank).limit(2).toList();
            return HandRank.of(HandRank.Type.THREE_OF_A_KIND, threeRank, kickers.get(0), kickers.get(1));
        }

        if (Arrays.equals(counts, new int[]{1, 2, 2})) {
            List<Integer> pairs = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 2).limit(2).toList();
            int kicker = ranksDesc.stream().filter(r -> !pairs.contains(r)).findFirst().get();
            return HandRank.of(HandRank.Type.TWO_PAIR, pairs.get(0), pairs.get(1), kicker);
        }

        if (Arrays.equals(counts, new int[]{1, 1, 1, 2})) {
            int pairRank = ranksDesc.stream().filter(r -> rankGroups.get(r).size() == 2).findFirst().get();
            List<Integer> kickers = ranksDesc.stream().filter(r -> r != pairRank).limit(3).toList();
            return HandRank.of(HandRank.Type.ONE_PAIR, pairRank, kickers.get(0), kickers.get(1), kickers.get(2));
        }

        // 高牌
        return HandRank.of(HandRank.Type.HIGH_CARD, ranksDesc.get(0), ranksDesc.get(1), ranksDesc.get(2), ranksDesc.get(3), ranksDesc.get(4));
    }

    // 判断是否为顺子（支持 A-2-3-4-5）
    private static boolean isStraight(List<Card> cards) {
        Set<Integer> ranks = cards.stream()
                .map(card -> card.getRank().getValue())
                .collect(Collectors.toSet());

        // 普通顺子
        for (int high = 14; high >= 5; high--) {
            boolean found = true;
            for (int i = 0; i < 5; i++) {
                if (!ranks.contains(high - i)) {
                    found = false;
                    break;
                }
            }
            if (found) return true;
        }

        // 特殊：A-2-3-4-5（A 当 1 用）
        if (ranks.contains(14) && ranks.contains(2) && ranks.contains(3) && ranks.contains(4) && ranks.contains(5)) {
            return true;
        }

        return false;
    }

    // 获取顺子中最高的牌（A-2-3-4-5 返回 5）
    private static int getHighestStraightCard(List<Card> cards) {
        Set<Integer> ranks = cards.stream()
                .map(card -> card.getRank().getValue())
                .collect(Collectors.toSet());

        // 先试普通顺子
        for (int high = 14; high >= 5; high--) {
            boolean found = true;
            for (int i = 0; i < 5; i++) {
                if (!ranks.contains(high - i)) {
                    found = false;
                    break;
                }
            }
            if (found) return high;
        }

        // 再试 wheel straight (A-2-3-4-5)
        if (ranks.contains(14) && ranks.contains(2) && ranks.contains(3) && ranks.contains(4) && ranks.contains(5)) {
            return 5;
        }

        throw new IllegalStateException("Not a straight!");
    }
}