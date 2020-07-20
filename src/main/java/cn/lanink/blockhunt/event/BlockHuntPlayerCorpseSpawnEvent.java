package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class BlockHuntPlayerCorpseSpawnEvent extends BlockHuntRoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public BlockHuntPlayerCorpseSpawnEvent(RoomBase room, Player player) {
        this.room = room;
        this.player = player;
    }

}
