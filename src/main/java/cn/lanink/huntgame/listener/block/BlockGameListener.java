package cn.lanink.huntgame.listener.block;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageBlock;
import cn.lanink.huntgame.entity.EntityCamouflageBlockDamage;
import cn.lanink.huntgame.room.EventType;
import cn.lanink.huntgame.room.PlayerData;
import cn.lanink.huntgame.room.PlayerIdentity;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.room.block.BlockInfo;
import cn.lanink.huntgame.room.block.BlockModeRoom;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class BlockGameListener extends BaseGameListener<BlockModeRoom> implements Listener {

    private final HuntGame huntGame = HuntGame.getInstance();

    private final HashSet<Player> clickToCool = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BlockModeRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
            if (player.getInventory().getItemInHand().getId() == 280) {
                if (this.clickToCool.contains(player)) {
                    return;
                }
                this.clickToCool.add(player);
                Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame,
                        () -> this.clickToCool.remove(player), 15);

                Block block = event.getBlock();
                Language language = this.huntGame.getLanguage(player);
                if (block.isNormalBlock()) {
                    BlockInfo blockInfo = new BlockInfo(block.getId(), block.getDamage());
                    room.getPlayerCamouflageBlockInfoMap().put(player, blockInfo);
                    room.getEntityCamouflageBlock(player).setBlockInfo(blockInfo);
                    Item blockItem = Item.get(block.getId(), block.getDamage());
                    blockItem.setCustomName(language.translateString("item-name-currentlyDisguisedBlock"));
                    player.getInventory().setItem(8, blockItem);
                    player.sendTitle("", language.translateString("subtitle-successfullySwitchedCamouflageBlocks"));

                    Item item = Tools.getHuntGameItem(20, player);
                    item.setCount(room.getCamouflageCoolingTime()); //5秒冷却
                    player.getInventory().setItem(0, item);

                    room.getPlayer(player).addEventCount(EventType.PREY_SWITCHED_CAMOUFLAGE);
                }else {
                    player.sendTitle("", language.translateString("subtitle-cannotPretendToBeThisBlock"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            BlockModeRoom room = this.getListenerRoom(damager.getLevel());
            if (room == null || !room.isPlaying(damager)) {
                return;
            }
            event.setCancelled(true);
            Entity entity = event.getEntity();
            Item item = damager.getInventory().getItemInHand();
            if ((room.getPlayer(damager).getIdentity() == PlayerIdentity.HUNTER || room.getPlayer(damager).getIdentity() == PlayerIdentity.CHANGE_HUNTER)
                    && item.getId() == 276 &&
                    entity instanceof EntityCamouflageBlockDamage) {
                Player player = ((EntityCamouflageBlockDamage) entity).getMaster();
                if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EntityCamouflageBlockDamage) {
            EntityCamouflageBlockDamage entity = (EntityCamouflageBlockDamage) event.getEntity();
            BlockModeRoom room = this.getListenerRoom(entity.getLevel());
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BlockModeRoom room = this.getListenerRoom(event.getTo().getLevel());
        Player player = event.getPlayer();
        if (room == null || room.getStatus() != RoomStatus.GAME || !room.isPlaying(player)) {
            return;
        }
        if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
            Level level = player.getLevel();
            Set<Player> players = new HashSet<>(room.getPlayers().keySet());
            players.remove(player);
            BlockInfo blockInfo = room.getPlayerCamouflageBlockInfo(player);
            Position newPos = event.getTo().add(0, 0.5, 0).floor();
            Block block = Block.get(blockInfo.getId(), blockInfo.getDamage(), newPos);
            newPos.x += 0.5;
            newPos.z += 0.5;
            room.getEntityCamouflageBlockDamage(player).setPosition(newPos);
            EntityCamouflageBlock entityCamouflageBlock = room.getEntityCamouflageBlock(player);
            if (entityCamouflageBlock.distance(newPos) > 0.85) {
                entityCamouflageBlock.setPosition(newPos);
                entityCamouflageBlock.respawnToAll();
            }
            HashSet<Vector3> set = new HashSet<>();
            set.add(block);
            set.add(event.getFrom().add(0, 0.5, 0).floor());
            set.add(event.getFrom().floor());
            level.sendBlocks(players.toArray(new Player[0]), set.toArray(new Vector3[0]));

            room.updateBlockList.add(player.clone());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.clickToCool.remove(event.getPlayer());
    }

}
