package com.example.dpoker.pojo;

import lombok.Value;

@Value
public class Card {
    Suit suit;
    Rank rank;
    public enum Suit{
        HEARTS('❤'), DIAMONDS('♦'),
        CLUBS('♣'), SPADES('♠');

        private final char symbol;
        Suit(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol(){return symbol;}
    }

    public enum Rank{
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6),
        SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(11), QUEEN(12), KING(13), ACE(14);

        private final int value;
        Rank(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString(){
            if (this.value > 10){
                return this.name().substring(0,1);
            }else {
                return Integer.toString(this.value);
            }
        }
    }

    @Override
    public String toString(){
        char symbol = this.suit.getSymbol();
        String rankString = this.rank.toString();
        return symbol+rankString;
    }

}
