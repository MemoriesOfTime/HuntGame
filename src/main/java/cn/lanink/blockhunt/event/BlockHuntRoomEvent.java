package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.BaseRoom;
import cn.nukkit.event.Event;

/**
 * @author lt_name
 */
public abstract class BlockHuntRoomEvent extends Event {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
