package cn.lanink.blockhunt.listener.classic;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageBlock;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.ClassicModeRoom;
import cn.lanink.blockhunt.utils.Tools;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lt_name
 */
public class ClassicGameListener extends BaseGameListener<ClassicModeRoom> implements Listener {

    private final BlockHunt blockHunt = BlockHunt.getInstance();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            BaseRoom room = this.getListenerRoom(damager.getLevel());
            if (room == null || !room.isPlaying(damager)) return;
            event.setCancelled(true);
            Entity entity = event.getEntity();
            Item item = damager.getInventory() != null ? damager.getInventory().getItemInHand() : null;
            if ((room.getPlayers(damager) == 2 || room.getPlayers(damager) == 12)
                    && item != null && item.getId() == 276 &&
                    entity instanceof EntityCamouflageBlock && entity.namedTag != null) {
                Player player = Server.getInstance().getPlayer(entity.namedTag.getString("playerName"));
                room.playerDamageEvent(damager, player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            player.setAllowModifyWorld(false);
        }
        if (room.getStatus() == 1) {
            CompoundTag tag = event.getItem() != null ? event.getItem().getNamedTag() : null;
            if (tag != null && tag.getBoolean("isBlockHuntItem") && tag.getInt("BlockHuntType") == 10) {
                room.quitRoom(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        ClassicModeRoom room = this.getListenerRoom(event.getTo().getLevel());
        if (room == null || room.getStatus() != 2) {
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || room.getStatus() != 2 || !room.isPlaying(player)) {
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
        if (player == null || event.getMessage() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
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
        if (player == null || event.getMessage() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player) || room.getStatus() != 2) return;
        String message = "§7[§a" + Tools.getStringIdentity(room, player) + "§7]§r " + player.getName() + " §b>>>§r " + event.getMessage();
        event.setMessage("");
        event.setCancelled(true);
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (entry.getValue() == room.getPlayers(player) ||
                    (room.getPlayers(player) == 2 && entry.getValue() == 12) ||
                    (room.getPlayers(player) == 12 && entry.getValue() ==2)) {
                entry.getKey().sendMessage(message);
            }
        }
    }

}
