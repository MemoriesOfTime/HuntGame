package cn.lanink.blockhunt.listener.animal;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageEntity;
import cn.lanink.blockhunt.room.RoomStatus;
import cn.lanink.blockhunt.room.animal.AnimalModeRoom;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;

/**
 * @author LT_Name
 */
public class AnimalGameListener extends BaseGameListener<AnimalModeRoom> {

    private final BlockHunt blockHunt = BlockHunt.getInstance();

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            AnimalModeRoom room = this.getListenerRoom(event.getEntity().getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            if (event.getRegainReason() == EntityRegainHealthEvent.CAUSE_EATING) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        AnimalModeRoom room = this.getListenerRoom(event.getEntity().getLevel());
        if (room == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!room.isPlaying(player)) {
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (room.getStatus() == RoomStatus.GAME) {
                    if (room.getPlayers(player) == 1) {
                        room.playerDeath(player);
                    }else {
                        player.teleport(room.getRandomSpawn().get(BlockHunt.RANDOM.nextInt(room.getRandomSpawn().size())));
                    }
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EntityCamouflageEntity) {
            EntityCamouflageEntity entity = (EntityCamouflageEntity) event.getEntity();
            AnimalModeRoom room = this.getListenerRoom(entity.getLevel());
            if (room == null) {
                return;
            }
            if (entity.getMaster() != null) {
                room.playerDeath(entity.getMaster());
                EntityDamageEvent cause = entity.getLastDamageCause();
                if (cause instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
                    if (damager instanceof Player) {
                        for (Player p : room.getPlayers().keySet()) {
                            p.sendMessage(this.blockHunt.getLanguage(p).huntersKillPrey
                                    .replace("%damagePlayer%", damager.getName())
                                    .replace("%player%", entity.getMaster().getName()));
                        }
                    }
                }
            }
        }
    }


}
