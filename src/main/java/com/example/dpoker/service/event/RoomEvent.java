package com.example.dpoker.service.event;

import com.example.dpoker.engine.BettingEngine;
import com.example.dpoker.pojo.GameRoom;

public interface RoomEvent {
    void handle(GameRoom room, BettingEngine bettingEngine);
}
