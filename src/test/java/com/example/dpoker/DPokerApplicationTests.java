package com.example.dpoker;

import com.example.dpoker.Utils.DeckUtils;
import com.example.dpoker.engine.GameEngine;
import com.example.dpoker.pojo.Card;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class DPokerApplicationTests {
    @Autowired
    private GameEngine engine;

//    @Test
//    void contextLoads() {
//        Player player1 = new Player(1,1000);
//        Player player2 = new Player(2,1000);
//        Player player3 = new Player(3,1000);
//        Player player4 = new Player(4,1000);
//
//        GameRoom gameRoom = new GameRoom(8794, new ArrayList<>(List.of(player1, player2, player3, player4)),"默认");
//
//
//        engine.startNewHand(gameRoom,10,20);
//
//        // 4. 验证结果
//        System.out.println("=== 游戏开局状态 ===");
//        System.out.println("底池: " + gameRoom.getTotalPotsAmount()); // 应为 30
//        System.out.println("当前最高下注: " + gameRoom.getCurrentBet()); // 应为 20
//
//        for (int i = 0; i < gameRoom.getPlayers().size(); i++) {
//            Player p = gameRoom.getPlayers().get(i);
//            String position = "";
//            if (i == gameRoom.getButtonIndex()){
//                position = "(庄家)";
//            } else if (i == (gameRoom.getButtonIndex()+1)%gameRoom.getPlayers().size()) {
//                position = "(小盲)";
//            }else if (i == (gameRoom.getButtonIndex()+2)%gameRoom.getPlayers().size()){
//                position = "(大盲)";
//            }
//            System.out.println("玩家" + p.getUserId() + position +
//                    " 筹码=" + p.getChips() +
//                    ", 底牌=" + p.getHoleCards());
//        }
//    }

    @Test
    public void testCompleteHand() {
        List<Player> players = Arrays.asList(
                new Player(1, 1000,"ee"),
                new Player(2, 1000,"ee2"),
                new Player(3, 1000,"e3"),
                new Player(4, 1000,"e4")
        );
        GameRoom room = new GameRoom(1001, players,"test");

        while (room.getPlayers().stream().noneMatch(player -> player.getChips() < 20)) {

            engine.runCompleteHand(room, 10, 20); // 自动跑完一局

            System.out.println("游戏结束！最终筹码:");
            int sum =0 ;
            for (Player p : players) {
                System.out.println("玩家" + p.getUserId() + ": " + p.getChips());
                sum+=p.getChips();
            }
            System.out.println("sum = " + sum);
        }
    }

    @Test
    public void show() {
        int n = 20;
        while (n>0) {
            List<Card> shuffledDeck = DeckUtils.createShuffledDeck();
            System.out.println(shuffledDeck +  "\n");
            n--;
        }
    }

}
