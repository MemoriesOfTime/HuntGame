package cn.lanink.huntgame.listener;

import cn.lanink.gamecore.utils.PlayerDataUtils;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.ui.GuiCreate;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.scheduler.Task;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * 玩家进入/退出服务器 或传送到其他世界时，退出房间
 *
 * @author lt_name
 */
@SuppressWarnings("unused")
public class PlayerJoinAndQuit implements Listener {

    private final HuntGame huntGame;

    public PlayerJoinAndQuit(HuntGame huntGame) {
        this.huntGame = huntGame;
    }

    @EventHandler
    public void onPlayerLogin(PlayerPreLoginEvent e) {
        String lang = e.getPlayer().getLoginChainData().getLanguageCode();
        HuntGame.getInstance().getPlayerLanguageHashMap().put(e.getPlayer(), lang);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.huntGame.getRooms().containsKey(player.getLevel().getName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame, new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        if (huntGame.isHasTips()) {
                            Tips.removeTipsConfig(player.getLevel().getName(), player);
                        }
                        File file = new File(huntGame.getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
                        if (file.exists()) {
                            PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player, file);
                            if (file.delete()) {
                                playerData.restoreAll();
                            }
                        }
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
        for (BaseRoom room : HuntGame.getInstance().getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
                break;
            }
        }
        this.huntGame.getPlayerLanguageHashMap().remove(player);
        GuiCreate.UI_CACHE.remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, BaseRoom> room =  this.huntGame.getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.huntGame.getLanguage(player).translateString("tpQuitRoomLevel"));
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.huntGame.getLanguage(player).translateString("tpJoinRoomLevel"));
            }
        }
    }

}
