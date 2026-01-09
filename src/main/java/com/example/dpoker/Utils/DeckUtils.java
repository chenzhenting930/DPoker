package com.example.dpoker.Utils;

import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckUtils {
    // 创建标准52张牌并洗牌
    public static List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck); // 随机打乱
        return deck;
    }

    public static void dealCard(Player player,List<Card> cards){
        player.getHoleCards().add(cards.remove(cards.size()-1));
    }
}
