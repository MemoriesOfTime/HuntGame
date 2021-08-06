package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.ui.GuiCreate;
import cn.lanink.blockhunt.utils.Tools;
import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
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

    private final BlockHunt blockHunt;

    public PlayerJoinAndQuit(BlockHunt blockHunt) {
        this.blockHunt = blockHunt;
    }

    @EventHandler
    public void onPlayerLogin(PlayerPreLoginEvent e) {
        String lang = e.getPlayer().getLoginChainData().getLanguageCode();
        BlockHunt.getInstance().getPlayerLanguageHashMap().put(e.getPlayer(), lang);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.blockHunt.getRooms().containsKey(player.getLevel().getName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        if (blockHunt.isHasTips()) {
                            Tips.removeTipsConfig(player.getLevel().getName(), player);
                        }
                        SavePlayerInventory.restore(BlockHunt.getInstance(), player);
                        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 1);
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
                room.quitRoom(player);
                break;
            }
        }
        this.blockHunt.getPlayerLanguageHashMap().remove(player);
        GuiCreate.UI_CACHE.remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, RoomBase> room =  this.blockHunt.getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.blockHunt.getLanguage(player).tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.blockHunt.getLanguage(player).tpJoinRoomLevel);
            }
        }
    }

}
