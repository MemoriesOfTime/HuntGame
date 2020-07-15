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
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
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
    @Override
    public synchronized void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.mode == 0) {
                this.initTask();
            }
            this.addPlaying(player);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(player);
            player.getInventory().setItem(8, Tools.getBlockHuntItem(10, player));
            if (player.teleport(this.getWaitSpawn())) {
                if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                player.sendMessage(this.blockHunt.getLanguage(player).joinRoom
                        .replace("%name%", this.level.getName()));
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
    @Override
    public synchronized void quitRoom(Player player) {
        this.quitRoom(player, true);
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     * @param online 是否在线
     */
    @Override
    public synchronized void quitRoom(Player player, boolean online) {
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
    @Override
    public synchronized void gameStart() {
        if (this.mode == 2) return;
        Tools.cleanEntity(this.getLevel(), true);
        this.setMode(2);
        this.assignIdentity();
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            if (this.getPlayers(player) == 2) continue;
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
    @Override
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
        int time = this.gameTime - (this.getSetGameTime() - 60);
        if (time >= 0) {
            if (time <= 5) {
                Tools.addSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                Item[] armor = new Item[4];
                armor[0] = Item.get(306);
                armor[1] = Item.get(307);
                armor[2] = Item.get(308);
                armor[3] = Item.get(309);
                for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
                    if (entry.getValue() == 2) {
                        entry.getKey().teleport(this.getRandomSpawn().get(
                                new Random().nextInt(this.getRandomSpawn().size())));
                        entry.getKey().getInventory().setArmorContents(armor);
                        entry.getKey().getInventory().addItem(Item.get(276));
                    }
                }
            }
        }
        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            if (this.getSurvivorPlayerNumber() <= 0) {
                this.victory(2);
            }
        }else {
            this.victory(1);
        }
        //复活
        for (Map.Entry<Player, Integer> entry : this.playerRespawnTime.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() == 0) {
                    this.playerRespawn(entry.getKey());
                }
            }
        }
    }

    /**
     * 分配玩家身份
     */
    @Override
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
                player.sendTitle(this.blockHunt.getLanguage(player).titleHuntersTitle,
                        this.blockHunt.getLanguage(player).titleHuntersSubtitle, 10, 40, 10);
                continue;
            }
            this.players.put(player, 1);
            player.sendTitle(this.blockHunt.getLanguage(player).titlePreyTitle,
                    this.blockHunt.getLanguage(player).titlePreySubtitle, 10, 40, 10);
        }
    }

    /**
     * 获取存活人数
     *
     * @return 存活人数
     */
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
    @Override
    public void playerDamage(Player damage, Player player) {
        if (this.getPlayers(player) == 1) {
            this.playerDeath(player);
        }
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    @Override
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

    @Override
    public void playerRespawn(Player player) {
        Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
            @Override
            public void onRun(int i) {
                for (Entity entity : level.getEntities()) {
                    if (entity instanceof EntityPlayerCorpse) {
                        if (entity.namedTag != null &&
                                entity.namedTag.getString("playerName").equals(player.getName())) {
                            entity.close();
                        }
                    }
                }
            }
        }, 1);
        this.players.put(player, 2);
        player.teleport(this.getRandomSpawn().get(
                new Random().nextInt(this.getRandomSpawn().size())));
        Tools.rePlayerState(player, true);
        Item[] armor = new Item[4];
        armor[0] = Item.get(306);
        armor[1] = Item.get(307);
        armor[2] = Item.get(308);
        armor[3] = Item.get(309);
        player.getInventory().setArmorContents(armor);
        player.getInventory().addItem(Item.get(276));
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    @Override
    public void playerCorpseSpawn(Player player) {
        this.playerRespawnTime.put(player, 20);
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
        nbt.putString("playerName", player.getName());
        EntityPlayerCorpse corpse = new EntityPlayerCorpse(player.getChunk(), nbt);
        corpse.setSkin(skin);
        corpse.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        corpse.setGliding(true);
        corpse.setRotation(player.getYaw(), 0);
        corpse.spawnToAll();
        corpse.updateMovement();
    }

}
