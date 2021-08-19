package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.event.player.PlayerEvent;

/**
 * @author lt_name
 */
public abstract class HuntGameRoomPlayerEvent extends PlayerEvent {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
