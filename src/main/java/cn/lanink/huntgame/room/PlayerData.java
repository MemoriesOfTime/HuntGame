package cn.lanink.huntgame.room;

import cn.nukkit.Player;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
@Getter
public class PlayerData {

    private final Player player;
    private PlayerIdentity identity = PlayerIdentity.NULL;
    private final ConcurrentHashMap<IntegralConfig.IntegralType, Integer> integral = new ConcurrentHashMap<>();

    public PlayerData(Player player) {
        this.player = player;
    }

    public void setIdentity(PlayerIdentity identity) {
        this.identity = identity;
    }

    public int getAllIntegral() {
        int i = 0;
        for (Integer integer : this.integral.values()) {
            i += integer;
        }
        return i;
    }

    public int getIntegral(IntegralConfig.IntegralType type) {
        return this.integral.getOrDefault(type, 0);
    }

    public void addIntegral(IntegralConfig.IntegralType type, int value) {
        this.integral.put(type, this.getIntegral(type) + value);
    }

}
