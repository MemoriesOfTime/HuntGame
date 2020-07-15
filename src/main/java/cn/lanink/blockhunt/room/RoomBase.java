package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.tasks.VictoryTask;
import cn.lanink.blockhunt.tasks.WaitTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */

public abstract class RoomBase {

    protected String gameMode = null;
    protected BlockHunt blockHunt = BlockHunt.getInstance();

    protected int mode; //0等待重置 1玩家等待中 2玩家游戏中 3胜利结算中
    protected final int setWaitTime, setGameTime;
    public int waitTime, gameTime; //秒
    protected final ArrayList<Position> randomSpawn = new ArrayList<>();
    protected final Position waitSpawn;
    protected final Level level;
    protected final LinkedHashMap<Player, Integer> players = new LinkedHashMap<>(); //0未分配 1猎物 2猎人
    protected final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomBase(Config config) {
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
        this.mode = 0;
        this.initTime();
    }

    public final void setGameName(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return this.gameMode;
    }

    /**
     * 初始化时间参数
     */
    public void initTime() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
    }

    /**
     * 初始化Task
     */
    protected void initTask() {
        if (this.mode != 1) {
            this.setMode(1);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.blockHunt, new WaitTask(this.blockHunt, this), 20);
        }
    }



    /**
     * @param mode 房间状态
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * @return 房间状态
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public abstract void joinRoom(Player player);

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
    public abstract void quitRoom(Player player, boolean online);

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
     * @deprecated
     * @param player 玩家
     * @return 玩家身份
     */
    public int getPlayerMode(Player player) {
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
    public Level getLevel() {
        return this.level;
    }

    /**
     * 房间开始游戏
     */
    public abstract void gameStart();

    /**
     * 结束本局游戏
     */
    public void endGame() {
        this.endGame(true);
    }

    /**
     * 结束本局游戏
     *
     * @param normal 正常关闭
     */
    public abstract void endGame(boolean normal);

    /**
     * 计时Task
     */
    public abstract void asyncTimeTask();

    /**
     * 分配玩家身份
     */
    public abstract void assignIdentity();

    /**
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public abstract int getSurvivorPlayerNumber();

    /**
     * 符合游戏条件的攻击
     *
     * @param damage 攻击者
     * @param player 被攻击者
     */
    public abstract void playerDamage(Player damage, Player player);

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public abstract void playerDeath(Player player);

    /**
     * 玩家复活
     *
     * @param player 玩家
     */
    public abstract void playerRespawn(Player player);

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public abstract void playerCorpseSpawn(Player player);

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected void victory(int victoryMode) {
        if (this.getPlayers().values().size() > 0) {
            this.setMode(3);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.blockHunt,
                    new VictoryTask(this.blockHunt, this, victoryMode), 20);
        }else {
            this.endGame();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomBase)) return false;
        RoomBase roomBase = (RoomBase) o;
        return gameMode.equals(roomBase.gameMode) &&
                level.equals(roomBase.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameMode, level);
    }

}
