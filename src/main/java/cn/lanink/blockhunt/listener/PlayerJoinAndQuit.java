package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.ui.GuiCreate;
import cn.lanink.blockhunt.utils.SavePlayerInventory;
import cn.lanink.blockhunt.utils.Tips;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.scheduler.Task;

import java.util.LinkedHashMap;

/**
 * 玩家进入/退出服务器 或传送到其他世界时，退出房间
 *
 * @author lt_name
 */
public class PlayerJoinAndQuit implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerPreLoginEvent e) {
        String lang = e.getPlayer().getLoginChainData().getLanguageCode();
        BlockHunt.getInstance().getPlayerLanguageHashMap().put(e.getPlayer(), lang);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && BlockHunt.getInstance().getRooms().containsKey(player.getLevel().getName())) {
            BlockHunt.getInstance().getServer().getScheduler().scheduleDelayedTask(BlockHunt.getInstance(), new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        if (Server.getInstance().getPluginManager().getPlugin("Tips") != null) {
                            Tips.removeTipsConfig(player.getLevel().getName(), player);
                        }
                        SavePlayerInventory.restore(player);
                        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 10);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        for (RoomBase room : BlockHunt.getInstance().getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player, false);
            }
        }
        BlockHunt.getInstance().getPlayerLanguageHashMap().remove(player);
        GuiCreate.UI_CACHE.remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, RoomBase> room =  BlockHunt.getInstance().getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(BlockHunt.getInstance().getLanguage(player).tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(BlockHunt.getInstance().getLanguage(player).tpJoinRoomLevel);
            }
        }
    }

}
