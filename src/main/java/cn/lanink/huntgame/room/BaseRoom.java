package cn.lanink.huntgame.room;

import cn.lanink.gamecore.utils.PlayerDataUtils;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityPlayerCorpse;
import cn.lanink.huntgame.event.*;
import cn.lanink.huntgame.tasks.VictoryTask;
import cn.lanink.huntgame.tasks.WaitTask;
import cn.lanink.huntgame.tasks.game.TimeTask;
import cn.lanink.huntgame.utils.Tools;
import cn.lanink.huntgame.utils.nsgb.HuntGameDataGamePlayerPojo;
import cn.nsgamebase.api.GbGameApi;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */

public abstract class BaseRoom extends RoomConfig {

    protected String gameMode = null;
    protected HuntGame huntGame = HuntGame.getInstance();

    @Setter
    @Getter
    protected RoomStatus status;

    public int waitTime;
    public int gameTime;

    @Getter
    protected final LinkedHashMap<Player, PlayerData> players = new LinkedHashMap<>();
    protected final ConcurrentHashMap<Player, Integer> playerRespawnTime = new ConcurrentHashMap<>();


    /**
     * 初始化
     *
     * @param level 游戏世界
     * @param config 配置文件
     */
    public BaseRoom(@NotNull Level level, @NotNull Config config) {
        super(level, config);

        this.status = RoomStatus.TASK_NEED_INITIALIZED;
        this.initData();

        for (String listenerName : this.getListeners()) {
            try {
                this.huntGame.getHuntGameListeners().get(listenerName).addListenerRoom(this);
            } catch (Exception e) {
                this.huntGame.getLogger().error("监听器添加房间时错误", e);
            }
        }
    }

    public final void setGameName(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return this.gameMode;
    }

    public List<String> getListeners() {
        ArrayList<String> list = new ArrayList<>();
        list.add("DefaultGameListener");
        return list;
    }

    /**
     * 初始化时间参数
     */
    public void initData() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.playerRespawnTime.clear();
    }

    /**
     * 初始化Task
     */
    protected void initTask() {
        if (this.status != RoomStatus.WAIT) {
            this.setStatus(RoomStatus.WAIT);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.huntGame, new WaitTask(this.huntGame, this), 20);
        }
    }

    public boolean canJoin() {
        return (this.getStatus() == RoomStatus.TASK_NEED_INITIALIZED || this.getStatus() == RoomStatus.WAIT) &&
                this.getPlayers().size() < this.getMaxPlayers();
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public synchronized void joinRoom(Player player) {
        if (this.players.size() < this.getMaxPlayers()) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame, () -> {
                if (this.isPlaying(player) && player.getLevel() != this.level) {
                    this.quitRoom(player);
                }
            }, 1);
            if (this.status == RoomStatus.TASK_NEED_INITIALIZED) {
                this.initTask();
            }
            this.addPlaying(player);
            Tools.rePlayerState(player, true);

            PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player);
            playerData.saveAll();
            playerData.saveToFile(new File(this.huntGame.getDataFolder() + "/PlayerInventory/" + player.getName() + ".json"));

            player.getInventory().clearAll();
            player.getOffhandInventory().clearAll();
            player.getUIInventory().clearAll();
            player.getEnderChestInventory().clearAll();

            player.getInventory().setItem(8, Tools.getHuntGameItem(10, player));
            player.teleport(this.getWaitSpawn());
            if (this.huntGame.isHasTips()) {
                Tips.closeTipsShow(this.level.getName(), player);
            }
            player.sendMessage(this.huntGame.getLanguage(player).translateString("joinRoom")
                    .replace("%name%", this.level.getName()));
        }
    }

    public synchronized void quitRoom(Player player) {
        this.quitRoom(player, true);
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     * @param initiative 玩家主动退出
     */
    public synchronized void quitRoom(Player player, boolean initiative) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (this.huntGame.isHasTips()) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        this.players.keySet().forEach(player::showPlayer);
        this.players.keySet().forEach(p -> p.showPlayer(player));
        this.huntGame.getScoreboard().closeScoreboard(player);
        Tools.rePlayerState(player, false);

        player.getInventory().clearAll();
        player.getOffhandInventory().clearAll();
        player.getUIInventory().clearAll();
        player.getEnderChestInventory().clearAll();

        File file = new File(this.huntGame.getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        if (file.exists()) {
            PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player, file);
            if (file.delete()) {
                playerData.restoreAll();
            }
        }

        this.players.keySet().forEach(p -> p.showPlayer(player));

        if (this.huntGame.isAutomaticNextRound() && !initiative) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame, () -> Server.getInstance().dispatchCommand(player, this.huntGame.getCmdUser() + " join mode:" + this.getGameMode()), 10);
        }else {
            if (this.huntGame.getConfig().exists("QuitRoom.cmd")) {
                Tools.executeCommands(player, this.huntGame.getConfig().getStringList("QuitRoom.cmd"));
            }
        }
    }

    /**
     * 记录在游戏内的玩家
     *
     * @param player 玩家
     */
    public void addPlaying(Player player) {
        if (!this.players.containsKey(player)) {
            this.players.put(player, new PlayerData(player));
        }
    }

    /**
     * @return boolean 玩家是否在游戏里
     * @param player 玩家
     */
    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * 获取玩家身份
     *
     * @param player 玩家
     * @return 身份
     */
    public PlayerData getPlayer(Player player) {
        return this.players.get(player);
    }

    public PlayerIdentity getPlayerIdentity(Player player) {
        return this.players.get(player).getIdentity();
    }

    /**
     * 房间开始游戏
     */
    public synchronized void gameStart() {
        if (this.status == RoomStatus.GAME) {
            return;
        }

        Server.getInstance().getPluginManager().callEvent(new HuntGameRoomStartEvent(this));

        this.setStatus(RoomStatus.GAME);

        Tools.cleanEntity(this.getLevel(), true);

        this.assignIdentity();

        for (Player player: this.players.keySet()) {
            if (this.getPlayerIdentity(player) != PlayerIdentity.PREY) {
                continue;
            }

            player.getInventory().setItem(0, Tools.getHuntGameItem(3, player));
            Item item = Tools.getHuntGameItem(20, player);
            item.setCount(64);
            player.getInventory().setItem(1, Tools.getHuntGameItem(1, player));
            Item arrow = Item.get(262);
            arrow.setCount(5);
            player.getInventory().setItem(2, arrow);
            player.getInventory().setItem(4, item);
            player.getInventory().setItem(5, item);
            player.getInventory().setItem(6, item);
            player.getInventory().setItem(7, item);

            player.setScale(1);
        }

        Server.getInstance().getScheduler().scheduleRepeatingTask(this.huntGame,
                new TimeTask(this.huntGame, this), 20);
    }

    /**
     * 结束本局游戏
     */
    public synchronized void endGame() {
        this.endGame(PlayerIdentity.NULL);
    }

    public synchronized void endGame(PlayerIdentity victory) {
        victory = victory == PlayerIdentity.CHANGE_HUNTER ? PlayerIdentity.HUNTER : victory;
        HuntGameRoomEndEvent ev = new HuntGameRoomEndEvent(this, victory);
        Server.getInstance().getPluginManager().callEvent(ev);

        this.setStatus(RoomStatus.ROOM_PREPARE);

        HashMap<Player, Integer> victoryPlayers = new HashMap<>();
        HashMap<Player, Integer> defeatPlayers = new HashMap<>();
        for (Map.Entry<Player, PlayerData> entry : this.players.entrySet()) {
            this.players.keySet().forEach(player -> entry.getKey().showPlayer(player));
            switch (victory) {
                case PREY:
                    if (entry.getValue().getIdentity() == PlayerIdentity.PREY) {
                        victoryPlayers.put(entry.getKey(), entry.getValue().getAllIntegral());
                    }else {
                        defeatPlayers.put(entry.getKey(), entry.getValue().getAllIntegral());
                    }
                    break;
                case HUNTER:
                    if (entry.getValue().getIdentity() == PlayerIdentity.HUNTER) {
                        victoryPlayers.put(entry.getKey(), entry.getValue().getAllIntegral());
                    }else {
                        defeatPlayers.put(entry.getKey(), entry.getValue().getAllIntegral());
                    }
                    break;
            }
        }

        for (Player player : new ArrayList<>(this.players.keySet())) {
            this.quitRoom(player, false);
        }
        this.initData();

        Tools.cleanEntity(this.getLevel(), true);

        //所有玩家退出房间后再给奖励，防止物品被清
        if (!victoryPlayers.isEmpty() || !defeatPlayers.isEmpty()) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame, () -> {
                victoryPlayers.forEach((player, points) -> {
                    Tools.executeCommands(player, huntGame.getVictoryCmd());
                    Tools.executeCommands(player, huntGame.getRewardCmd(points));
                    if (this.huntGame.isHasNsGB()) {
                        Map<String, Integer> integerMap = this.huntGame.getfapReward(points);
                        HuntGameDataGamePlayerPojo pojo = new HuntGameDataGamePlayerPojo();
                        pojo.add("played");
                        int money = integerMap.get("money");
                        int point = integerMap.get("point");
                        int exp = integerMap.get("exp");
                        int maxMultiplier = integerMap.get("maxMultiplier");
                        GbGameApi.saveAndReward(player.getName(), "HuntGame", pojo, money, exp, maxMultiplier);
                    }
                });
                defeatPlayers.forEach((player, points) -> {
                    Tools.executeCommands(player, huntGame.getDefeatCmd());
                    Tools.executeCommands(player, huntGame.getRewardCmd(points));
                    if (this.huntGame.isHasNsGB()) {
                        Map<String, Integer> integerMap = this.huntGame.getfapReward(points);
                        HuntGameDataGamePlayerPojo pojo = new HuntGameDataGamePlayerPojo();
                        pojo.add("played");
                        int money = integerMap.get("money");
                        int point = integerMap.get("point");
                        int exp = integerMap.get("exp");
                        int maxMultiplier = integerMap.get("maxMultiplier");
                        GbGameApi.saveAndReward(player.getName(), "HuntGame", pojo, money, exp, maxMultiplier);
                    }
                });
            }, 1);
        }

        this.setStatus(RoomStatus.TASK_NEED_INITIALIZED);
    }

    /**
     * 计时Task
     */
    public void timeTask() {
        int time = this.gameTime - (this.getSetGameTime() - (HuntGame.debug ? 5 : 60));
        if (time >= 0) {
            this.players.keySet().forEach(player -> player.sendTip(this.huntGame.getLanguage(player)
                    .translateString("huntersDispatchedTimeBottom").replace("time%", time + "")));
            if (time%10 == 0) {
                for (Map.Entry<Player, PlayerData> entry : this.players.entrySet()) {
                    if (entry.getValue().getIdentity() == PlayerIdentity.HUNTER) {
                        entry.getKey().addEffect(Effect.getEffect(15).setDuration(400).setVisible(false)); //失明
                        entry.getKey().addEffect(Effect.getEffect(2).setAmplifier(2).setDuration(400).setVisible(false)); //缓慢2
                    }else {
                        entry.getKey().addEffect(Effect.getEffect(1).setDuration(400).setVisible(false)); //速度提升
                    }
                }
            }
            if (time <= 5) {
                Tools.addSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                for (Map.Entry<Player, PlayerData> entry : this.players.entrySet()) {
                    entry.getKey().removeAllEffects();
                    if (entry.getValue().getIdentity() == PlayerIdentity.HUNTER) {
                        entry.getKey().teleport(randomSpawn.get(Tools.RANDOM.nextInt(randomSpawn.size())));
                        this.giveHuntItem(entry.getKey());
                    }
                }
            }
        }else if (this.gameTime%5 == 0) {
            for (Map.Entry<Player, PlayerData> entry : this.getPlayers().entrySet()) {
                if (entry.getValue().getIdentity() == PlayerIdentity.HUNTER || entry.getValue().getIdentity() == PlayerIdentity.CHANGE_HUNTER) {
                    entry.getKey().addEffect(Effect.getEffect(1).setDuration(400).setVisible(false)); //速度提升1
                } else if (entry.getValue().getIdentity() == PlayerIdentity.PREY) {
                    entry.getKey().addEffect(Effect.getEffect(2).setDuration(400).setVisible(false)); //缓慢1
                }
            }
        }

        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            int x = 0;
            boolean hasHunter = false;
            for (PlayerData playerData : this.players.values()) {
                switch (playerData.getIdentity()) {
                    case PREY:
                        x++;
                        break;
                    case HUNTER:
                    case CHANGE_HUNTER:
                        hasHunter = true;
                        break;
                }
            }
            if (!hasHunter) {
                this.victory(PlayerIdentity.PREY);
            }else if (x <= 0) {
                this.victory(PlayerIdentity.HUNTER);
            }
        }else {
            this.victory(PlayerIdentity.PREY);
        }

        //复活
        for (Map.Entry<Player, Integer> entry : this.playerRespawnTime.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                entry.getKey().sendTip(this.huntGame.getLanguage(entry.getKey())
                        .translateString("respawnTimeBottom").replace("%time%", entry.getValue() + ""));
                if (entry.getValue() == 0) {
                    this.playerRespawn(entry.getKey());
                }
            }
        }

        for (Map.Entry<Player, PlayerData> entry : this.getPlayers().entrySet()) {
            if (this.gameTime%5 == 0) {
                entry.getKey().getFoodData().addFoodLevel(1, 0);
            }
            //道具
            PlayerInventory inventory = entry.getKey().getInventory();
            Item coolingItem = Tools.getHuntGameItem(20, entry.getKey());
            Item item = inventory.getItem(0);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() - 1);
                    inventory.setItem(0, item);
                }else {
                    inventory.setItem(0, Tools.getHuntGameItem(3, entry.getKey()));
                }
            }

            item = inventory.getItem(4);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() - 1);
                } else {
                    item = Tools.getHuntGameItem(21, entry.getKey());
                }
                inventory.setItem(4, item);
            }

            item = inventory.getItem(5);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() - 1);
                } else {
                    item = Tools.getHuntGameItem(22, entry.getKey());
                }
                inventory.setItem(5, item);
            }else {
                Item coolingItem2 = Tools.getHuntGameItem(30, entry.getKey());
                if (coolingItem2.equals(item)) {
                    if (item.getCount() > 1) {
                        item.setCount(item.getCount() - 1);
                    } else {
                        item = Tools.getHuntGameItem(31, entry.getKey());
                    }
                    inventory.setItem(5, item);
                }
            }

            item = inventory.getItem(6);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() - 1);
                } else {
                    item = Tools.getHuntGameItem(23, entry.getKey());
                }
                inventory.setItem(6, item);
            }

            item = inventory.getItem(7);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() - 1);
                } else {
                    item = Tools.getHuntGameItem(24, entry.getKey());
                }
                inventory.setItem(7, item);
            }
        }
    }

    /**
     * 给玩家猎人的物品
     *
     * @param player 玩家
     */
    protected void giveHuntItem(Player player) {
        Item[] armor = new Item[4];
        armor[0] = Item.get(306).setNamedTag(new CompoundTag().putByte("Unbreakable", 1)); //不会受损的盔甲
        armor[1] = Item.get(307).setNamedTag(new CompoundTag().putByte("Unbreakable", 1));
        armor[2] = Item.get(308).setNamedTag(new CompoundTag().putByte("Unbreakable", 1));
        armor[3] = Item.get(309).setNamedTag(new CompoundTag().putByte("Unbreakable", 1));
        player.getInventory().setArmorContents(armor);
        player.getInventory().setItem(0, Tools.getHuntGameItem(2, player));
        player.getInventory().setItem(1, Tools.getHuntGameItem(1, player));
        player.getInventory().setItem(2, Item.get(262));
        player.getInventory().setItem(5, Tools.getHuntGameItem(31, player));
    }

    /**
     * 分配玩家身份
     */
    public void assignIdentity() {
        LinkedHashMap<Player, PlayerData> players = this.getPlayers();
        int random = Tools.RANDOM.nextInt(players.size()) + 1;
        int x = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            x++;
            if (x == random) {
                this.getPlayer(player).setIdentity(PlayerIdentity.HUNTER);
                player.sendTitle(this.huntGame.getLanguage(player).translateString("titleHuntersTitle"),
                        this.huntGame.getLanguage(player).translateString("titleHuntersSubtitle"), 10, 40, 10);
                continue;
            }
            this.getPlayer(player).setIdentity(PlayerIdentity.PREY);
            player.sendTitle(this.huntGame.getLanguage(player).translateString("titlePreyTitle"),
                    this.huntGame.getLanguage(player).translateString("titlePreySubtitle"), 10, 40, 10);
        }
    }

    /**
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (PlayerData playerData : this.getPlayers().values()) {
            if (playerData.getIdentity() == PlayerIdentity.PREY) {
                x++;
            }
        }
        return x;
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public void playerDeath(Player player) {
        if (this.getPlayer(player).getIdentity() == PlayerIdentity.NULL) {
            return;
        }

        HuntGamePlayerDeathEvent ev = new HuntGamePlayerDeathEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        if (this.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
            this.playerRespawnTime.put(player, 20);
            this.playerCorpseSpawn(player);

            this.level.sendBlocks(this.players.keySet().toArray(new Player[0]), new Vector3[] { player.floor() });
        }
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
        player.setGamemode(Player.SPECTATOR);
        this.getPlayer(player).setIdentity(PlayerIdentity.NULL);
        Tools.addSound(this, Sound.GAME_PLAYER_HURT);
    }

    /**
     * 玩家复活
     *
     * @param player 玩家
     */
    protected void playerRespawn(Player player) {
        HuntGamePlayerRespawnEvent ev = new HuntGamePlayerRespawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        for (Entity entity : this.level.getEntities()) {
            if (entity instanceof EntityPlayerCorpse) {
                if (entity.namedTag != null &&
                        entity.namedTag.getString("playerName").equals(player.getName())) {
                    entity.close();
                }
            }
        }
        this.getPlayer(player).setIdentity(PlayerIdentity.CHANGE_HUNTER);
        this.players.keySet().forEach(p -> p.showPlayer(player));
        player.teleport(this.randomSpawn.get(Tools.RANDOM.nextInt(this.randomSpawn.size())));
        Tools.rePlayerState(player, true);
        Tools.setPlayerInvisible(player, false);

        this.giveHuntItem(player);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public void playerCorpseSpawn(Player player) {
        HuntGamePlayerCorpseSpawnEvent ev = new HuntGamePlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        Skin skin = player.getSkin();
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = this.huntGame.getDefaultSkin();
        }
        CompoundTag nbt = Entity.getDefaultNBT(player);
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
    }

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected synchronized void victory(PlayerIdentity victoryMode) {
        victoryMode = victoryMode == PlayerIdentity.CHANGE_HUNTER ? PlayerIdentity.HUNTER : victoryMode;
        if (!this.getPlayers().isEmpty()) {
            this.setStatus(RoomStatus.VICTORY);
            for (Player player : this.players.keySet()) {
                player.getUIInventory().clearAll();
                player.getInventory().clearAll();
                player.getInventory().setItem(8, Tools.getHuntGameItem(10, player));
                this.getPlayer(player).addEventCount(EventType.COMPLETE_GAME);
            }
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.huntGame,
                    new VictoryTask(this.huntGame, this, victoryMode), 20);
        }else {
            this.endGame();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseRoom)) return false;
        BaseRoom baseRoom = (BaseRoom) o;
        return level.equals(baseRoom.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

}
