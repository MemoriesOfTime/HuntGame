package cn.lanink.huntgame.listener.animal;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.room.animal.AnimalModeRoom;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.*;

/**
 * @author LT_Name
 */
@SuppressWarnings("unused")
public class AnimalGameListener extends BaseGameListener<AnimalModeRoom> {

    private final HuntGame huntGame = HuntGame.getInstance();

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
                        player.teleport(room.getRandomSpawn().get(HuntGame.RANDOM.nextInt(room.getRandomSpawn().size())));
                    }
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }
            if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM && !(event instanceof EntityDamageByChildEntityEvent)) {
                event.setCancelled(true);
            }else if (event.getFinalDamage() + 1 > player.getHealth()) {
                event.setDamage(0);
                room.playerDeath(player);
            }
        }else if (event.getEntity() instanceof EntityCamouflageEntity) {
            EntityCamouflageEntity entity = (EntityCamouflageEntity) event.getEntity();
            if (entity.getMaster() == null) {
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            AnimalModeRoom room = this.getListenerRoom(damager.getLevel());
            if (room == null) {
                return;
            }
            if ((room.getPlayers(damager) == 2 || room.getPlayers(damager) == 12) &&
                    (damager.getInventory().getItemInHand().getId() != 276 && !(event instanceof EntityDamageByChildEntityEvent))) {
                event.setCancelled(true);
                return;
            }
            if (event.getEntity() instanceof Player) {
                Player player = ((Player) event.getEntity());
                if (room.getPlayers(player) == 1 && (room.getPlayers(damager) == 2 || room.getPlayers(damager) == 12)) {
                    player.attack(new EntityDamageEvent(damager, EntityDamageEvent.DamageCause.CUSTOM, event.getDamage()));
                    damager.setHealth(Math.min(damager.getHealth() + 4, damager.getMaxHealth()));
                }else if ((room.getPlayers(player) == 2 || room.getPlayers(player) == 12) && room.getPlayers(damager) == 1) {
                    event.setDamage(0);
                    event.setKnockBack(event.getKnockBack() * 1.5f);
                }
            }else if (event.getEntity() instanceof EntityCamouflageEntity) {
                EntityCamouflageEntity entity = (EntityCamouflageEntity) event.getEntity();
                if (room.getPlayers(damager) == 2 || room.getPlayers(damager) == 12) {
                    if (entity.getMaster() == null) {
                        damager.attack(new EntityDamageEvent(damager, EntityDamageEvent.DamageCause.CUSTOM, 1));
                    }else {
                        damager.setHealth(Math.min(damager.getHealth() + 4, damager.getMaxHealth()));
                    }
                }else {
                    event.setCancelled(true);
                    if (damager.getInventory().getItemInHand().getId() == 280) {
                        if (room.getPlayerCamouflageEntity().get(damager).getEntityName().equals(entity.getEntityName())) {
                            damager.sendTitle("", "你已经伪装成 " + entity.getEntityName() + " 了！不能重复伪装");
                        }else {
                            room.getPlayerCamouflageEntity().get(damager).close();

                            EntityCamouflageEntity newEntity =
                                    EntityCamouflageEntity.create(damager.chunk, Entity.getDefaultNBT(damager), entity.getEntityName());
                            room.getPlayerCamouflageEntity().put(damager, newEntity);
                            newEntity.setMaster(damager);
                            newEntity.hidePlayer(damager);
                            newEntity.spawnToAll();

                            damager.sendTitle("", "你伪装成了 " + entity.getEntityName() + " ！");
                        }
                    }
                }
            }
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
                            p.sendMessage(this.huntGame.getLanguage(p).translateString("huntersKillPrey")
                                    .replace("%damagePlayer%", damager.getName())
                                    .replace("%player%", entity.getMaster().getName()));
                        }
                    }
                }
            }
        }
    }


}
