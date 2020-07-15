package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.entity.EntityPlayerCorpse;
import cn.lanink.blockhunt.tasks.game.TimeTask;
import cn.lanink.blockhunt.tasks.game.TipsTask;
import cn.lanink.blockhunt.utils.SavePlayerInventory;
import cn.lanink.blockhunt.utils.Tips;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class RoomClassicMode extends RoomBase {

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomClassicMode(Config config) {
        super(config);
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.mode == 0) {
                this.initTask();
            }
            this.addPlaying(player);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(player);
            if (player.teleport(this.getWaitSpawn())) {
                if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                /*player.sendMessage(MurderMystery.getInstance().getLanguage().joinRoom
                        .replace("%name%", this.level.getName()));*/
            }else {
                this.quitRoom(player, true);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    public void quitRoom(Player player) {
        this.quitRoom(player, true);
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     * @param online 是否在线
     */
    public void quitRoom(Player player, boolean online) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        if (online) {
            this.blockHunt.getScoreboard().closeScoreboard(player);
            player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
            Tools.rePlayerState(player, false);
            SavePlayerInventory.restore(player);
        }
    }

    /**
     * 房间开始游戏
     */
    public void gameStart() {
        Tools.cleanEntity(this.getLevel(), true);
        this.setMode(2);
        this.assignIdentity();
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
        }
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.blockHunt, new TimeTask(this.blockHunt, this), 20,true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.blockHunt, new TipsTask(this.blockHunt, this), 18, true);
    }

    /**
     * 结束本局游戏
     * @param normal 正常关闭
     */
    public synchronized void endGame(boolean normal) {
        mode = 0;
        if (normal) {
            Iterator<Map.Entry<Player, Integer>> it = players.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Player, Integer> entry = it.next();
                it.remove();
                quitRoom(entry.getKey());
            }
        }else {
            getLevel().getPlayers().values().forEach(
                    player -> player.kick(this.blockHunt.getLanguage(player).roomSafeKick));
        }
        initTime();
        Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
            @Override
            public void onRun(int i) {
                Tools.cleanEntity(getLevel(), true);
            }
        }, 1);
    }

    /**
     * 计时Task
     */
    @Override
    public void asyncTimeTask() {

    }

    /**
     * 分配玩家身份
     */
    public void assignIdentity() {
        LinkedHashMap<Player, Integer> players = this.getPlayers();
        int random = new Random().nextInt(players.size()) + 1;
        int x = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            x++;
            if (x == random) {
                this.players.put(player, 2);
                player.sendTitle(this.blockHunt.getLanguage(player).titleDetectiveTitle,
                        this.blockHunt.getLanguage(player).titleDetectiveSubtitle, 10, 40, 10);
                continue;
            }
            this.players.put(player, 1);
            player.sendTitle(this.blockHunt.getLanguage(player).titleCommonPeopleTitle,
                    this.blockHunt.getLanguage(player).titleCommonPeopleSubtitle, 10, 40, 10);
        }
    }

    @Override
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (Integer integer : this.getPlayers().values()) {
            if (integer == 1) {
                x++;
            }
        }
        return x;
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damage 攻击者
     * @param player 被攻击者
     */
    public void playerDamage(Player damage, Player player) {
        if (this.getPlayers(player) == 0) return;

    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public void playerDeath(Player player) {
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
        player.setGamemode(3);
        this.players.put(player, 0);
        Tools.setPlayerInvisible(player, true);
        Tools.addSound(this, Sound.GAME_PLAYER_HURT);
        this.playerCorpseSpawn(player);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public void playerCorpseSpawn(Player player) {
        Skin skin = player.getSkin();
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = this.blockHunt.getCorpseSkin();
        }
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        EntityPlayerCorpse corpse = new EntityPlayerCorpse(player.getChunk(), nbt);
        corpse.setSkin(skin);
        corpse.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        corpse.setGliding(true);
        corpse.setRotation(player.getYaw(), 0);
        corpse.spawnToAll();
        corpse.updateMovement();
    }

}
