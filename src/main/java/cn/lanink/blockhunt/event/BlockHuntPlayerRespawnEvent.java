package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class BlockHuntPlayerRespawnEvent extends BlockHuntRoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public BlockHuntPlayerRespawnEvent(BaseRoom room, Player player) {
        this.room = room;
        this.player = player;
    }

}
