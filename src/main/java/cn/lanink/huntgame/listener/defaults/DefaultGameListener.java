package cn.lanink.huntgame.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * @author LT_Name
 */
@SuppressWarnings("unused")
public class DefaultGameListener extends BaseGameListener<BaseRoom> {

    private final HuntGame huntGame = HuntGame.getInstance();

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
            if (tag != null && tag.getBoolean("isHuntGameItem") && tag.getInt("HuntGameType") == 10) {
                room.quitRoom(player);
                event.setCancelled(true);
            }
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
        if (HuntGame.debug) {
            return; //debug模式不拦截命令
        }
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getMessage().startsWith(this.huntGame.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.huntGame.getCmdAdmin(), 1)) {
            return;
        }
        for (String string : this.huntGame.getCmdWhitelist()) {
            if (string.equalsIgnoreCase(event.getMessage())) {
                return;
            }
        }
        event.setMessage("");
        event.setCancelled(true);
        player.sendMessage(this.huntGame.getLanguage(player).translateString("useCmdInRoom"));
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
