package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.event.Event;

/**
 * @author lt_name
 */
public abstract class BlockHuntRoomEvent extends Event {

    protected RoomBase room;

    public RoomBase getRoom() {
        return this.room;
    }

}
