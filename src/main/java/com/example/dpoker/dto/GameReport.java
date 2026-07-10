package com.example.dpoker.dto;

import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GameReport {
    private Integer userId;
    private String playerName;
    private float point; // 用户积分
    private int chips; // 用户筹码
    private int totalBetInHand; // 整手牌累计投入底池的筹码
    private int winChips;
    private int loseChips;
    private List<Card> holeCards; // 底牌（2张）

    public static List<GameReport> generateGameReport(GameRoom room){
        List<GameReport> gameReportList = new ArrayList<>();
        if (room.isGameEnded()){
            List<Player> players = room.getPlayers();
            for (Player player : players) {
                GameReport gameReport = new GameReport();
                gameReport.setUserId(player.getUserId());
                gameReport.setPoint(player.getPoint());
                gameReport.setHoleCards(player.getHoleCards());
                gameReport.setPlayerName(player.getPlayerName());
                gameReport.setChips(player.getChips());
                gameReport.setTotalBetInHand(player.getTotalBetInHand());
                Map<Player, Integer> winnerMap = room.getWinnerMap();
                if (winnerMap != null && winnerMap.containsKey(player)){
                    Integer amount = winnerMap.get(player);
                    gameReport.setWinChips(amount);
                    gameReport.setLoseChips(0);
                }else {
                    gameReport.setLoseChips(player.getTotalBetInHand());
                    gameReport.setWinChips(0);
                }


                gameReportList.add(gameReport);
            }
        }

        return gameReportList;

    }

}
