package com.example.dpoker.service.event;

import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.engine.BettingEngine;
import com.example.dpoker.pojo.GameRoom;
import com.example.dpoker.pojo.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Slf4j
public class PlayerActionEvent implements RoomEvent {

    private ActionRequest action;

    public PlayerActionEvent(ActionRequest action) {
        this.action = action;
    }

    @Override
    public void handle(GameRoom room,BettingEngine bettingEngine) {
        if (room.getCurrentPlayer().getUserId().equals(action.getPlayerId())){
            bettingEngine.handleAction(
                    room,
                    action.getPlayerId(),
                    action.getAction(),
                    action.getAmount()
            );
            log.info("Player {} action: {} amount:{}", action.getPlayerId(), action.getAction(),action.getAmount());
        }else {
            throw new IllegalStateException("还没轮到你操作！");
        }


    }
}

