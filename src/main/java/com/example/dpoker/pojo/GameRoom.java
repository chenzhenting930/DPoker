package com.example.dpoker.pojo;

import com.example.dpoker.Utils.DeckUtils;
import com.example.dpoker.dto.GameRoomVO;
import com.example.dpoker.service.event.RoomEvent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Data
public class GameRoom {
    private Integer roomId;
    private String name; //房间名称
    /**
     * 按座位顺序
     */
    private List<Player> players;
    private List<Card> communityCards;      // 公共牌（flop/turn/river）
    private List<Card> deck;                // 剩余牌堆（用于发牌）
    private int pot;                        // 当前底池
    private int currentBet;                 // 当前轮次最高下注额
    private int round;                      // 0=preflop, 1=flop,2=turn,3=river,4=showdown
    private boolean gameEnded = true;
    private boolean gameStarted = false;
    private int buttonIndex;
    private int currentPlayerIndex = -1;        // 当前轮到谁行动（index）
    private List<Integer> bettingOrder;         // 本轮下注顺序（玩家 index 列表）
    private boolean roundCompleted = false;     // 当前轮次是否结束（临时标志）
    private List<Pot> pots = new ArrayList<>();
    Integer[] blinds;
    private List<Player> winners;
    private List<Integer> winAmounts;
    Map<Player,Integer> winnerMap;

    //阻塞队列，用于接收用户操作
    private final BlockingQueue<RoomEvent> queue = new LinkedBlockingQueue<>();
    public boolean enqueue(RoomEvent event) {
        return queue.offer(event);
    }
    public BlockingQueue<RoomEvent> getQueue() {
        return queue;
    }

    public void resetGameRoomForANewGame(){
        communityCards.clear();
        deck = DeckUtils.createShuffledDeck();
        currentBet=0;
        round=0;
        gameEnded =false;
        buttonIndex = (buttonIndex+1)%players.size();
        roundCompleted = false;
    }

    public GameRoom(Integer roomId, List<Player> initialPlayers,String name) {
        this.roomId = roomId;
        this.name = name;
        this.players = new ArrayList<>(initialPlayers); // 复制一份，避免外部修改
        this.communityCards = new ArrayList<>();
//        this.pot = 0;
        this.currentBet = 0;
        this.round = 0;
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

    public Player getCurrentPlayer(){
        int currentPlayerIndex = getCurrentPlayerIndex();
        if (currentPlayerIndex == -1) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }


    public void removePlayer(Integer playerId) {
        players.removeIf(player -> player.getUserId().equals(playerId));
    }
    public Player getPlayerById(Integer playerId) {
        return players.stream()
                .filter(player -> player.getUserId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean isAllPlayersReady() {
        return players.stream().allMatch(Player::isReady);
    }

    public GameRoomVO toGameRoomVO(){
        return GameRoomVO.builder()
                .roomId(roomId)
                .name(name)
                .gameStarted(gameStarted)
                .players(players.stream().map(Player::toPlayerVO).collect(Collectors.toList()))
                .blinds(blinds)
                .build();
    }
}
