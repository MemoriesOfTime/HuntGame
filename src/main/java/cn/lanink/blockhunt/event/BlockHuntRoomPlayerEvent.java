package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.event.player.PlayerEvent;

/**
 * @author lt_name
 */
public abstract class BlockHuntRoomPlayerEvent extends PlayerEvent {

    protected RoomBase room;

    public RoomBase getRoom() {
        return this.room;
    }

}
