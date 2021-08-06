package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.BaseRoom;
import cn.nukkit.event.player.PlayerEvent;

/**
 * @author lt_name
 */
public abstract class BlockHuntRoomPlayerEvent extends PlayerEvent {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
