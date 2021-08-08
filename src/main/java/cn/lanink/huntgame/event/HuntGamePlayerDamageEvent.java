package cn.lanink.huntgame.event;

import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author lt_name
 */
public class HuntGamePlayerDamageEvent extends HuntGameRoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player damager;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public HuntGamePlayerDamageEvent(BaseRoom room, Player damager, Player player) {
        this.room = room;
        this.damager = damager;
        this.player = player;
    }

    public Player getDamager() {
        return this.damager;
    }

}
