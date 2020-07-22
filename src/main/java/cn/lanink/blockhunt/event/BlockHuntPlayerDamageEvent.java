package cn.lanink.blockhunt.event;

import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class BlockHuntPlayerDamageEvent extends BlockHuntRoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player damager;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public BlockHuntPlayerDamageEvent(RoomBase room, Player damager, Player player) {
        this.room = room;
        this.damager = damager;
        this.player = player;
    }

    public Player getDamager() {
        return this.damager;
    }

}
