package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.entity.EntityCamouflageEntity;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

/**
 * @author LT_Name
 */
public class AnimalModeRoom extends BaseRoom {

    @Getter
    protected final HashMap<Player, EntityCamouflageEntity> playerCamouflageEntity = new HashMap<>();

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public AnimalModeRoom(Config config) {
        super(config);
    }

    @Override
    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.add("AnimalGameListener");
        return list;
    }

    @Override
    public void initData() {
        super.initData();
        if (this.playerCamouflageEntity != null) {
            this.playerCamouflageEntity.clear();
        }
    }

    @Override
    public void gameStart() {
        super.gameStart();
        int x = 0;
        for (Player player: this.players.keySet()) {
            if (this.getPlayers(player) != 1) {
                //continue;
            }
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            //player.teleport(this.getRandomSpawn().get(x));
            x++;
            EntityCamouflageEntity camouflageEntity =
                    new EntityCamouflageEntity(player.chunk, Entity.getDefaultNBT(player), 12);
            this.playerCamouflageEntity.put(player, camouflageEntity);
            camouflageEntity.setMaster(player);
            camouflageEntity.hidePlayer(player);
            camouflageEntity.spawnToAll();

            new EntityCamouflageEntity(player.chunk, Entity.getDefaultNBT(player), 12).spawnToAll();
        }
    }

    @Override
    protected void endGame(boolean normal, int victory) {

    }

    @Override
    public void asyncTimeTask() {

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
