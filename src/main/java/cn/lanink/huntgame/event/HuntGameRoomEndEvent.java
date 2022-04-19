package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.PlayerIdentity;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class HuntGameRoomEndEvent extends HuntGameRoomEvent {

    private PlayerIdentity victory;

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public HuntGameRoomEndEvent(BaseRoom room, PlayerIdentity victory) {
        this.room = room;
        this.victory = victory;
    }

    public void setVictory(PlayerIdentity victory) {
        this.victory = victory;
    }

    public PlayerIdentity getVictory() {
        return this.victory;
    }

}
