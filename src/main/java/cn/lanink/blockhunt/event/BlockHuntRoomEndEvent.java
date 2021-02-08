package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class BlockHuntRoomEndEvent extends BlockHuntRoomEvent {

    private int victory;

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public BlockHuntRoomEndEvent(RoomBase room, int victory) {
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
