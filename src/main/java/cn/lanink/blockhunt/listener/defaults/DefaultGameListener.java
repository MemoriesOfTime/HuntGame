package cn.lanink.blockhunt.listener.defaults;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.RoomStatus;
import cn.lanink.blockhunt.utils.Tools;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * @author LT_Name
 */
public class DefaultGameListener extends BaseGameListener<BaseRoom> {

    private final BlockHunt blockHunt = BlockHunt.getInstance();

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
        if (room.getStatus() == RoomStatus.WAIT) {
            CompoundTag tag = event.getItem() != null ? event.getItem().getNamedTag() : null;
            if (tag != null && tag.getBoolean("isBlockHuntItem") && tag.getInt("BlockHuntType") == 10) {
                room.quitRoom(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) return;
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
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || room.getStatus() != RoomStatus.GAME || !room.isPlaying(player)) {
            return;
        }
        //猎人不能卸下盔甲
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
        if (BlockHunt.debug) {
            return; //debug模式不拦截命令
        }
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
        if (room == null || !room.isPlaying(player) || room.getStatus() != RoomStatus.GAME) {
            return;
        }
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
