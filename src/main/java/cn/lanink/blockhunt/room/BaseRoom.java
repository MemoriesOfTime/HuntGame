package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.event.*;
import cn.lanink.blockhunt.tasks.VictoryTask;
import cn.lanink.blockhunt.tasks.WaitTask;
import cn.lanink.blockhunt.tasks.game.TimeTask;
import cn.lanink.blockhunt.utils.Tools;
import cn.lanink.gamecore.room.IRoom;
import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */

public abstract class BaseRoom implements IRoom {

    protected String gameMode = null;
    protected BlockHunt blockHunt = BlockHunt.getInstance();

    @Setter
    @Getter
    protected RoomStatus status;
    protected final int setWaitTime;
    protected final int setGameTime;
    public int waitTime;
    public int gameTime;
    protected final ArrayList<Position> randomSpawn = new ArrayList<>();
    protected final Position waitSpawn;
    protected final Level level;
    protected final LinkedHashMap<Player, Integer> players = new LinkedHashMap<>(); //0未分配 1猎物 2猎人 12猎物转化成的猎人
    protected final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();


    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public BaseRoom(Config config) {
        this.level = Server.getInstance().getLevelByName(config.getString("world"));
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        String[] s1 = config.getString("waitSpawn").split(":");
        this.waitSpawn = new Position(Integer.parseInt(s1[0]),
                Integer.parseInt(s1[1]),
                Integer.parseInt(s1[2]),
                this.getLevel());
        for (String string : config.getStringList("randomSpawn")) {
            String[] s = string.split(":");
            this.randomSpawn.add(new Position(
                    Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]),
                    this.level));
        }

        this.status = RoomStatus.TASK_NEED_INITIALIZED;
        this.initData();

        for (String listenerName : this.getListeners()) {
            try {
                this.blockHunt.getBlockHuntListeners().get(listenerName).addListenerRoom(this);
            } catch (Exception e) {
                this.blockHunt.getLogger().error("监听器添加房间时错误", e);
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
                    this.blockHunt, new WaitTask(this.blockHunt, this), 20);
        }
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public synchronized void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.status == RoomStatus.TASK_NEED_INITIALIZED) {
                this.initTask();
            }
            this.addPlaying(player);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(this.blockHunt, player);
            player.getInventory().setItem(8, Tools.getBlockHuntItem(10, player));
            if (player.teleport(this.getWaitSpawn())) {
                if (this.blockHunt.isHasTips()) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                player.sendMessage(this.blockHunt.getLanguage(player).joinRoom
                        .replace("%name%", this.level.getName()));
            }else {
                this.quitRoom(player);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    public synchronized void quitRoom(Player player) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (this.blockHunt.isHasTips()) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        this.players.keySet().forEach(player::showPlayer);
        this.players.keySet().forEach(p -> p.showPlayer(player));
        this.blockHunt.getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(this.blockHunt, player);
        this.players.keySet().forEach(p -> p.showPlayer(player));
    }

    /**
     * 记录在游戏内的玩家
     *
     * @param player 玩家
     */
    public void addPlaying(Player player) {
        if (!this.players.containsKey(player)) {
            this.addPlaying(player, 0);
        }
    }

    /**
     * 记录在游戏内的玩家
     *
     * @param player 玩家
     * @param mode 身份
     */
    public void addPlaying(Player player, Integer mode) {
        this.players.put(player, mode);
    }

    /**
     * @return boolean 玩家是否在游戏里
     * @param player 玩家
     */
    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * @return 玩家列表
     */
    public LinkedHashMap<Player, Integer> getPlayers() {
        return this.players;
    }

    /**
     * 获取玩家身份
     *
     * @param player 玩家
     * @return 身份
     */
    public int getPlayers(Player player) {
        if (isPlaying(player)) {
            return this.players.get(player);
        }else {
            return 0;
        }
    }

    /**
     * @return 出生点
     */
    public Position getWaitSpawn() {
        return this.waitSpawn;
    }

    /**
     * @return 随机出生点列表
     */
    public List<Position> getRandomSpawn() {
        return this.randomSpawn;
    }

    /**
     * @return 等待时间
     */
    public int getSetWaitTime() {
        return this.setWaitTime;
    }

    /**
     * @return 游戏时间
     */
    public int getSetGameTime() {
        return this.setGameTime;
    }

    /**
     * @return 游戏世界
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getLevelName() {
        return this.getLevel().getName();
    }

    public final void gameStartEvent() {
        Server.getInstance().getPluginManager().callEvent(new BlockHuntRoomStartEvent(this));
        this.gameStart();
    }

    /**
     * 房间开始游戏
     */
    public synchronized void gameStart() {
        if (this.status == RoomStatus.GAME) return;
        this.setStatus(RoomStatus.GAME);
        Tools.cleanEntity(this.getLevel(), true);
        this.assignIdentity();

        Server.getInstance().getScheduler().scheduleRepeatingTask(this.blockHunt,
                new TimeTask(this.blockHunt, this), 20);
    }

    /**
     * 结束本局游戏
     */
    public synchronized void endGameEvent() {
        this.endGameEvent(true, 0);
    }

    public final void endGameEvent(boolean normal, int victory) {
        BlockHuntRoomEndEvent ev = new BlockHuntRoomEndEvent(this, victory);
        Server.getInstance().getPluginManager().callEvent(ev);
        this.endGame(normal, ev.getVictory());
    }

    protected synchronized void endGame(boolean normal, int victory) {
        this.status = RoomStatus.TASK_NEED_INITIALIZED;
        HashSet<Player> victoryPlayers = new HashSet<>();
        HashSet<Player> defeatPlayers = new HashSet<>();
        for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
            players.keySet().forEach(player -> entry.getKey().showPlayer(player));
            switch (victory) {
                case 1:
                    if (entry.getValue() == 1) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                    break;
                case 2:
                    if (entry.getValue() == 2) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                    break;
            }
        }
        for (Player player : new ArrayList<>(players.keySet())) {
            this.quitRoom(player);
        }
        this.initData();
        Tools.cleanEntity(getLevel(), true);
        Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
            @Override
            public void onRun(int i) {
                //所有玩家退出房间后再给奖励，防止物品被清
                victoryPlayers.forEach(player -> Tools.cmd(player, blockHunt.getVictoryCmd()));
                defeatPlayers.forEach(player -> Tools.cmd(player, blockHunt.getDefeatCmd()));
            }
        }, 1);
    }

    /**
     * 计时Task
     */
    public abstract void asyncTimeTask();

    /**
     * 分配玩家身份
     */
    public void assignIdentity() {
        LinkedHashMap<Player, Integer> players = this.getPlayers();
        int random = BlockHunt.RANDOM.nextInt(players.size()) + 1;
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
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public abstract int getSurvivorPlayerNumber();

    public final void playerDamageEvent(Player damager, Player player) {
        BlockHuntPlayerDamageEvent ev = new BlockHuntPlayerDamageEvent(this, damager, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerDamage(damager, player);
        }
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damager 攻击者
     * @param player 被攻击者
     */
    protected abstract void playerDamage(Player damager, Player player);

    public final void playerDeathEvent(Player player) {
        BlockHuntPlayerDeathEvent ev = new BlockHuntPlayerDeathEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerDeath(player);
        }
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    protected abstract void playerDeath(Player player);

    public final void playerRespawnEvent(Player player) {
        BlockHuntPlayerRespawnEvent ev = new BlockHuntPlayerRespawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerRespawn(player);
        }
    }

    /**
     * 玩家复活
     *
     * @param player 玩家
     */
    protected abstract void playerRespawn(Player player);

    public final void playerCorpseSpawnEvent(Player player) {
        BlockHuntPlayerCorpseSpawnEvent ev = new BlockHuntPlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerCorpseSpawn(player);
        }
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    protected abstract void playerCorpseSpawn(Player player);

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected synchronized void victory(int victoryMode) {
        if (this.getPlayers().size() > 0) {
            this.setStatus(RoomStatus.VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.blockHunt,
                    new VictoryTask(this.blockHunt, this, victoryMode), 20);
        }else {
            this.endGameEvent();
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