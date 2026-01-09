package com.example.dpoker.pojo;

import com.example.dpoker.Utils.DeckUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class GameRoom {
    private Integer roomId;
    /**
     * 按座位顺序
     */
    private List<Player> players;
    private List<Card> communityCards;      // 公共牌（flop/turn/river）
    private List<Card> deck;                // 剩余牌堆（用于发牌）
    private int pot;                        // 当前底池
    private int currentBet;                 // 当前轮次最高下注额
    private int round;                      // 0=preflop, 1=flop,2=turn,3=river,4=showdown
    private boolean gameEnded = false;
    private int buttonIndex;
    private int currentPlayerIndex = -1;        // 当前轮到谁行动（index）
    private List<Integer> bettingOrder;         // 本轮下注顺序（玩家 index 列表）
    private boolean roundCompleted = false;     // 当前轮次是否结束（临时标志）
    private List<Pot> pots = new ArrayList<>();

    public void resetGameRoomForANewGame(){
        communityCards.clear();
        deck = DeckUtils.createShuffledDeck();
        currentBet=0;
        round=0;
        gameEnded =false;
        buttonIndex = (buttonIndex+1)%players.size();
        roundCompleted = false;
    }

    public GameRoom(Integer roomId, List<Player> initialPlayers) {
        this.roomId = roomId;
        this.players = new ArrayList<>(initialPlayers); // 复制一份，避免外部修改
        this.communityCards = new ArrayList<>();
//        this.pot = 0;
        this.currentBet = 0;
        this.round = 0;
        this.gameEnded = false;
        this.buttonIndex = players.size()-1;//初始化为最后一个玩家
    }

    public int getTotalPotsAmount(){
        return pots.stream().mapToInt(Pot::getAmount).sum();

    }
    public String getCurrentRound(){
        return switch (round) {
            case 0 -> "preflop";
            case 1 -> "flop";
            case 2 -> "turn";
            case 3 -> "river";
            case 4 -> "showdown";
            default -> "unknown";
        };
    }

    // 判断是否还有未弃牌的玩家
    public boolean hasActivePlayers() {
        return players.stream().anyMatch(Player::isActive);
    }

    public int getPlayerIndex(Player player){
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i) == player) {
                return i;
            }
        }
        throw new IllegalStateException("Player index not found");
    }
}
