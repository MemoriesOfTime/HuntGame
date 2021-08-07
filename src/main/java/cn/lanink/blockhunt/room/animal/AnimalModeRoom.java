package cn.lanink.blockhunt.room.animal;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageEntity;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
                continue;
            }
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
            EntityCamouflageEntity camouflageEntity =
                    EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), "Pig");
            this.playerCamouflageEntity.put(player, camouflageEntity);
            camouflageEntity.setMaster(player);
            camouflageEntity.hidePlayer(player);
            camouflageEntity.spawnToAll();

            if (BlockHunt.debug) {
                EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), "Pig").spawnToAll();
            }

            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }
    }

    @Override
    public void asyncTimeTask() {
        super.asyncTimeTask();

        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            LinkedList<String> ms = new LinkedList<>();
            for (String string : this.blockHunt.getLanguage(entry.getKey()).gameTimeScoreBoard.split("\n")) {
                ms.add(string.replace("%mode%", Tools.getStringIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.blockHunt.getScoreboard().showScoreboard(entry.getKey(), this.blockHunt.getLanguage(entry.getKey()).scoreBoardTitle, ms);
        }
    }

}
