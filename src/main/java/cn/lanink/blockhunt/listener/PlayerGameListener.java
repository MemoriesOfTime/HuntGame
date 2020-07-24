package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageBlock;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author lt_name
 */
public class PlayerGameListener implements Listener {

    private final BlockHunt blockHunt;

    public PlayerGameListener(BlockHunt blockHunt) {
        this.blockHunt = blockHunt;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            RoomBase room = this.blockHunt.getRooms().getOrDefault(damager.getLevel().getName(), null);
            if (room == null || !room.useDefaultListener() || !room.isPlaying(damager)) return;
            event.setCancelled(true);
            Entity entity = event.getEntity();
            Item item = damager.getInventory() != null ? damager.getInventory().getItemInHand() : null;
            if (room.getPlayers(damager) == 2 && item != null && item.getId() == 276 &&
                    entity instanceof EntityCamouflageBlock && entity.namedTag != null) {
                Player player = Server.getInstance().getPlayer(entity.namedTag.getString("playerName"));
                room.playerDamageEvent(damager, player);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            RoomBase room = this.blockHunt.getRooms().get(player.getLevel().getName());
            if (room == null || !room.useDefaultListener() || !room.isPlaying(player)) return;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RoomBase room = this.blockHunt.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.useDefaultListener()) return;
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            player.setAllowModifyWorld(false);
        }
        if (room.getStatus() == 1) {
            CompoundTag tag = event.getItem() != null ? event.getItem().getNamedTag() : null;
            if (tag != null && tag.getBoolean("isBlockHuntItem") && tag.getInt("BlockHuntType") == 10) {
                room.quitRoom(player, true);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        RoomBase room = this.blockHunt.getRooms().getOrDefault(event.getTo().getLevel().getName(), null);
        if (room == null || !room.useDefaultListener() || room.getStatus() != 2) return;
        Player player = event.getPlayer();
        if (room.getPlayers(player) == 1) {
            Level level = player.getLevel();
            Set<Player> players = new HashSet<>(room.getPlayers().keySet());
            players.remove(player);
            Integer[] integers = room.getPlayerCamouflageBlock(player);
            Block block = Block.get(integers[0], integers[1], event.getTo().floor());
            Vector3 vector3 = event.getTo().floor();
            vector3.x += 0.5;
            vector3.z += 0.5;
            room.getEntityCamouflageBlocks(player).setPosition(vector3);
            level.sendBlocks(players.toArray(new Player[0]), new Vector3[] {
                    event.getFrom().floor(), block });
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        RoomBase room = this.blockHunt.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.useDefaultListener() || room.getStatus() != 2 || !room.isPlaying(player)) {
            return;
        }
        if (event.getSlot() >= event.getInventory().getSize() ||
                (room.getPlayers(player) == 1 && event.getSlot() == 8)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) return;
        RoomBase room = this.blockHunt.getRooms().get(player.getLevel().getName());
        if (room == null || !room.useDefaultListener() || !room.isPlaying(player)) return;
        if (event.getMessage().startsWith(this.blockHunt.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.blockHunt.getCmdAdmin(), 1)) {
            return;
        }
        event.setMessage("");
        event.setCancelled(true);
        player.sendMessage(this.blockHunt.getLanguage(player).useCmdInRoom);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) return;
        RoomBase room = this.blockHunt.getRooms().get(player.getLevel().getName());
        if (room == null || !room.useDefaultListener() || !room.isPlaying(player) || room.getStatus() != 2) return;
        String message = "§7[§a" + Tools.getStringIdentity(room, player) + "§7]§r " + player.getName() + " §b>>>§r " + event.getMessage();
        event.setMessage("");
        event.setCancelled(true);
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (entry.getValue() == room.getPlayers(player)) {
                entry.getKey().sendMessage(message);
            }
        }
    }

}
