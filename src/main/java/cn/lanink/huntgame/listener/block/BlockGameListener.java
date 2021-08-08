package cn.lanink.huntgame.listener.block;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageBlock;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.room.block.BlockModeRoom;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lt_name
 */
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
        if (room.getPlayers(player) == 1) {
            if (player.getInventory().getItemInHand().getId() == 280) {
                if (this.clickToCool.contains(player)) {
                    return;
                }
                this.clickToCool.add(player);
                Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame,
                        () -> this.clickToCool.remove(player), 10);

                Block block = event.getBlock();
                if (block.isNormalBlock()) {
                    room.getPlayerCamouflageBlock().put(player, new Integer[]{ block.getId(), block.getDamage() });
                    Item blockItem = Item.get(block.getId(), block.getDamage());
                    blockItem.setCustomName("当前伪装的方块");
                    player.getInventory().setItem(8, blockItem);
                    player.sendTitle("", "切换伪装方块成功！");
                }else {
                    player.sendTitle("", "无法伪装成此方块！");
                }
            }
        }
    }

    @EventHandler
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
            if ((room.getPlayers(damager) == 2 || room.getPlayers(damager) == 12)
                    && item.getId() == 276 &&
                    entity instanceof EntityCamouflageBlock) {
                Player player = ((EntityCamouflageBlock) entity).getMaster();
                if (room.getPlayers(player) == 1) {
                    room.playerDeath(player);
                    for (Player p : room.getPlayers().keySet()) {
                        p.sendMessage(this.huntGame.getLanguage(p).huntersKillPrey
                                .replace("%damagePlayer%", damager.getName())
                                .replace("%player%", player.getName()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        BaseRoom room = this.getListenerRoom(event.getEntity().getLevel());
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
            event.setCancelled(true);
        }else {
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BlockModeRoom room = this.getListenerRoom(event.getTo().getLevel());
        if (room == null || room.getStatus() != RoomStatus.GAME) {
            return;
        }
        Player player = event.getPlayer();
        if (room.getPlayers(player) == 1) {
            Level level = player.getLevel();
            Set<Player> players = new HashSet<>(room.getPlayers().keySet());
            players.remove(player);
            Integer[] integers = room.getPlayerCamouflageBlock(player);
            Position newPos = event.getTo().add(0, 0.5, 0).floor();
            Block block = Block.get(integers[0], integers[1], newPos);
            newPos.x += 0.5;
            newPos.z += 0.5;
            room.getEntityCamouflageBlocks(player).setPosition(newPos);
            level.sendBlocks(players.toArray(new Player[0]), new Vector3[] {
                    event.getFrom().add(0, 0.5, 0).floor(), block });
        }
    }

}
