package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class HuntGamePlayerCorpseSpawnEvent extends HuntGameRoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public HuntGamePlayerCorpseSpawnEvent(BaseRoom room, Player player) {
        this.room = room;
        this.player = player;
    }

}
