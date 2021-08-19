package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.event.Event;

/**
 * @author lt_name
 */
public abstract class HuntGameRoomEvent extends Event {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
