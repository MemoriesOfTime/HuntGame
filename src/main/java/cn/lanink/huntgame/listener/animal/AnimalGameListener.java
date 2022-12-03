package cn.lanink.huntgame.listener.animal;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.EntityCamouflageEntityDamage;
import cn.lanink.huntgame.room.EventType;
import cn.lanink.huntgame.room.PlayerData;
import cn.lanink.huntgame.room.PlayerIdentity;
import cn.lanink.huntgame.room.animal.AnimalModeRoom;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.*;
import cn.nukkit.item.Item;

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
        if (event.getEntity() instanceof EntityCamouflageEntityDamage) {
            EntityCamouflageEntityDamage entity = (EntityCamouflageEntityDamage) event.getEntity();
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
            PlayerIdentity identity = room.getPlayer(damager).getIdentity();
            if ((identity == PlayerIdentity.HUNTER || identity == PlayerIdentity.CHANGE_HUNTER) &&
                    (damager.getInventory().getItemInHand().getId() != 276 && !(event instanceof EntityDamageByChildEntityEvent))) {
                event.setCancelled(true);
                return;
            }
            /*if (event.getEntity() instanceof Player) {
                Player player = ((Player) event.getEntity());
                if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY && (identity == PlayerIdentity.HUNTER || identity == PlayerIdentity.CHANGE_HUNTER)) {
                    player.attack(new EntityDamageEvent(damager, EntityDamageEvent.DamageCause.CUSTOM, event.getDamage()));
                    damager.setHealth(Math.min(damager.getHealth() + 4, damager.getMaxHealth()));
                }
            }else */if (event.getEntity() instanceof EntityCamouflageEntityDamage) {
                EntityCamouflageEntityDamage entity = (EntityCamouflageEntityDamage) event.getEntity();
                if (identity == PlayerIdentity.HUNTER || identity == PlayerIdentity.CHANGE_HUNTER) {
                    if (entity.getMaster() == null) {
                        damager.attack(new EntityDamageEvent(damager, EntityDamageEvent.DamageCause.CUSTOM, 1));
                    }else {
                        damager.setHealth(Math.min(damager.getHealth() + 4, damager.getMaxHealth()));
                    }
                }else {
                    event.setCancelled(true);
                    Language language = this.huntGame.getLanguage(damager);
                    if (damager.getInventory().getItemInHand().getId() == 280) {
                        if (room.getPlayerCamouflageEntityDamageMap().get(damager).getEntityName().equals(entity.getEntityName())) {
                            damager.sendTitle("", language.translateString("subtitle-CanNotDisguiseAnimalsRepeatedly", entity.getEntityName()));
                        }else {
                            room.getPlayerCamouflageEntityDamageMap().get(damager).close();
                            EntityCamouflageEntityDamage newEntity =
                                    EntityCamouflageEntityDamage.create(damager.chunk, Entity.getDefaultNBT(damager), entity.getEntityName());
                            room.getPlayerCamouflageEntityDamageMap().put(damager, newEntity);
                            newEntity.setMaster(damager);
                            newEntity.hidePlayer(damager);
                            newEntity.spawnToAll();

                            room.getPlayerCamouflageEntityMap().get(damager).close();
                            EntityCamouflageEntity entityCamouflageEntity = EntityCamouflageEntity.create(damager.chunk, Entity.getDefaultNBT(damager), entity.getEntityName());
                            room.getPlayerCamouflageEntityMap().put(damager, entityCamouflageEntity);
                            entityCamouflageEntity.setMaster(damager);
                            entityCamouflageEntity.spawnToAll();

                            damager.sendTitle("", language.translateString("subtitle-camouflageAnimalSuccess", entity.getEntityName()));

                            Item item = Tools.getHuntGameItem(20, damager);
                            item.setCount(room.getCamouflageCoolingTime()); //5秒冷却
                            damager.getInventory().setItem(0, item);

                            room.getPlayer(damager).addEventCount(EventType.PREY_SWITCHED_CAMOUFLAGE);
                        }
                    }
                }
            }else if (event.getEntity() instanceof EntityCamouflageEntity) {
                event.setDamage(0);
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EntityCamouflageEntityDamage) {
            EntityCamouflageEntityDamage entity = (EntityCamouflageEntityDamage) event.getEntity();
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
                        PlayerData damagerPlayerData = room.getPlayer((Player) damager);
                        damagerPlayerData.addEventCount(EventType.HUNTER_KILL_PREY);
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
