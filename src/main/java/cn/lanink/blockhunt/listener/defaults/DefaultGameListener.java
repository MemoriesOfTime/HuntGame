package cn.lanink.blockhunt.listener.defaults;

import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageEvent;

import java.util.Random;

/**
 * @author LT_Name
 */
public class DefaultGameListener extends BaseGameListener<BaseRoom> {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (room.getStatus() == 2) {
                    if (room.getPlayers(player) == 1) {
                        room.playerDeathEvent(player);
                    }else {
                        player.teleport(room.getRandomSpawn().get(new Random().nextInt(room.getRandomSpawn().size())));
                    }
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }
            event.setCancelled(true);
        }
    }

}
