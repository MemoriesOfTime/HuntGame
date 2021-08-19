package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class HuntGameRoomEndEvent extends HuntGameRoomEvent {

    private int victory;

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public HuntGameRoomEndEvent(BaseRoom room, int victory) {
        this.room = room;
        this.victory = victory;
    }

    public void setVictory(int victory) {
        this.victory = victory;
    }

    public int getVictory() {
        return this.victory;
    }

}
