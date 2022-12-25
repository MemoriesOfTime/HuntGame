package cn.lanink.huntgame.room;

import cn.lanink.gamecore.room.IRoom;
import cn.lanink.huntgame.HuntGame;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LT_Name
 */
public class RoomConfig implements IRoom {

    protected HuntGame huntGame = HuntGame.getInstance();

    protected final Level level;

    @Getter
    protected int minPlayers;
    @Getter
    protected int maxPlayers;

    protected final int setWaitTime;
    protected final int setGameTime;

    protected final ArrayList<Position> randomSpawn = new ArrayList<>();

    protected final Position waitSpawn;

    public RoomConfig(@NotNull Level level, @NotNull Config config) {
        this.level = level;

        this.minPlayers = config.getInt("minPlayers", 3);
        if (this.minPlayers < 2) {
            this.minPlayers = 2;
        }
        this.maxPlayers = config.getInt("maxPlayers", 16);
        if (this.maxPlayers < this.minPlayers) {
            this.maxPlayers = this.minPlayers;
        }

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

    /**
     * @return 猎物伪装切换冷却时间（秒）
     */
    public int getCamouflageCoolingTime () {
        return 5;
    }

}
