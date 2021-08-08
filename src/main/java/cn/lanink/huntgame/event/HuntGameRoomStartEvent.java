package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class HuntGameRoomStartEvent extends HuntGameRoomEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public HuntGameRoomStartEvent(BaseRoom room) {
        this.room = room;
    }

}
