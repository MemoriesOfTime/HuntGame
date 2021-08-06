package cn.lanink.blockhunt.room;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

/**
 * @author LT_Name
 */
public class AnimalModeRoom extends BaseRoom {

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public AnimalModeRoom(Config config) {
        super(config);
    }

    @Override
    public void gameStart() {

    }

    @Override
    protected void endGame(boolean normal, int victory) {

    }

    @Override
    public void asyncTimeTask() {

    }

    @Override
    public void assignIdentity() {

    }

    @Override
    public int getSurvivorPlayerNumber() {
        return 0;
    }

    @Override
    protected void playerDamage(Player damager, Player player) {

    }

    @Override
    protected void playerDeath(Player player) {

    }

    @Override
    protected void playerRespawn(Player player) {

    }

    @Override
    protected void playerCorpseSpawn(Player player) {

    }

}
