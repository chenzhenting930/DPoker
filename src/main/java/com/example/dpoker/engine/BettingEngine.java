package com.example.dpoker.engine;

import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BettingEngine {
    public void handleAction(GameRoom room,int userId, String action, Integer amount) {
        Player player = findActivePlayer(room, userId);
        int currentIndex = getCurrentPlayerIndex(room, userId);

        switch (action.toLowerCase()) {
            case "fold" -> handleFold(player);
            case "check" -> handleCheck(player, room.getCurrentBet());
            case "call" -> handleCall(player, room);
            case "raise" -> {
                //这里的amount指这一轮该玩家总共下了多少筹码
                if (amount == null || amount <= room.getCurrentBet()) {
                    throw new IllegalArgumentException("Raise amount must be > current bet");
                }
                handleRaise(player, room, amount);
            }
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        }

        log.info("玩家 {} 执行了行动 {}", player, action);
        // 标记该玩家已完成本轮行动（从 bettingOrder 移除）
        room.getBettingOrder().remove(Integer.valueOf(currentIndex));

        // 检查是否本轮结束
        if (isBettingRoundComplete(room)) {
            room.setRoundCompleted(true);
        } else {
            // 更新下一个行动玩家
            updateCurrentPlayer(room);
        }
    }

    private void updateCurrentPlayer(GameRoom room) {
        if (!room.getBettingOrder().isEmpty()){
            room.setCurrentPlayerIndex(room.getBettingOrder().get(0));
        }else {
            room.setRoundCompleted(true);
        }
    }

    private boolean isBettingRoundComplete(GameRoom room) {
        boolean allActivePlayersHaveActed = room.getBettingOrder().isEmpty();
        boolean allBetsMatch = room.getPlayers().stream().filter(Player::isActive)
                .allMatch(player -> player.hasMetCurrentBet(room.getCurrentBet()));
        return allActivePlayersHaveActed && allBetsMatch;
    }

    private void handleRaise(Player player, GameRoom room, int raiseTo) {
        int additional = raiseTo - player.getBetThisRound();
        if (additional >= player.getChips()){
            raiseTo = player.getChips()+player.getBetThisRound();
            player.setAllIn(true);

            System.out.println("玩家 " + player.getUserId() + " 全下 (" + player.getChips() + ")");
        }
        additional = raiseTo - player.getBetThisRound();
        player.setBetThisRound(raiseTo);
        player.setChips(player.getChips()-additional);
        player.addToTotalBet(additional);
        if(room.getCurrentBet() < raiseTo){
            room.setCurrentBet(raiseTo);
        }


        room.setBettingOrder(buildNewBettingOrderAfterRaise(room, player));
    }

    private List<Integer> buildNewBettingOrderAfterRaise(GameRoom room, Player player) {
        int raiserIndex = room.getPlayerIndex(player);
        List<Integer> newOrder = new ArrayList<>();
        int n = room.getPlayers().size();
        for (int i = 1; i < n; i++) { // 从下家开始
            int idx = (raiserIndex + i) % n;
            Player p = room.getPlayers().get(idx);
            if (p.isActive() && p.getBetThisRound() < room.getCurrentBet() & !p.isAllIn()) {
                newOrder.add(idx);
            }
        }
        return newOrder;
    }

    private void handleCall(Player player, GameRoom room) {
        int toCall = room.getCurrentBet() - player.getBetThisRound();
        if (toCall >= player.getChips() ){
            // 自动全下
            toCall = player.getChips();
            player.setAllIn(true);
            System.out.println("玩家 " + player.getUserId() + " 全下 (" + player.getChips() + ")");
        }
        player.setChips(player.getChips() - toCall);
        player.setBetThisRound(player.getBetThisRound() + toCall);
        player.addToTotalBet(toCall);
//        room.setPot(room.getPot() + toCall);
    }

    private void handleCheck(Player player, int currentBet) {
        if (player.getBetThisRound()!=currentBet){
            throw new IllegalStateException("Cannot check when facing a bet");
        }
    }

    private void handleFold(Player player) {
        player.setFolded(true);
    }

    private int getCurrentPlayerIndex(GameRoom room, int userId) {
        List<Player> players = room.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId().equals(userId)) {
                return i;
            }
        }
        throw new IllegalStateException("Player index not found");
    }


    private Player findActivePlayer(GameRoom room, int userId) {
        return room.getPlayers().stream()
                .filter(p -> p.getUserId() == userId && p.isActive())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Player not found or folded"));
    }
}
