package cn.lanink.huntgame.room;

import cn.nukkit.Player;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
@Getter
public class PlayerData {

    private final Player player;

    private PlayerIdentity identity = PlayerIdentity.NULL;

    private final ConcurrentHashMap<EventType, Integer> eventCountMap = new ConcurrentHashMap<>();

    public PlayerData(Player player) {
        this.player = player;
    }

    public void setIdentity(PlayerIdentity identity) {
        this.identity = identity;
    }

    public int getEventCount(EventType event) {
        return this.eventCountMap.getOrDefault(event, 0);
    }

    public void addEventCount(EventType event) {
        this.addEventCount(event, 1);
    }

    public void addEventCount(EventType event, int count) {
        this.eventCountMap.put(event, this.getEventCount(event) + count);
    }

}
