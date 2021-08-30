package cn.lanink.huntgame.room.animal;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.tasks.game.AnimalSpawnTask;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author LT_Name
 */
public class AnimalModeRoom extends BaseRoom {

    @Getter
    protected final HashMap<Player, EntityCamouflageEntity> playerCamouflageEntity = new HashMap<>();

    @Getter
    private final ConcurrentLinkedQueue<Position> animalSpawnList = new ConcurrentLinkedQueue<>();

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
        if (this.animalSpawnList != null) {
            this.animalSpawnList.clear();
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

            String randomEntityName = EntityData.getRandomEntityName();
            EntityCamouflageEntity camouflageEntity =
                    EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), randomEntityName);
            this.playerCamouflageEntity.put(player, camouflageEntity);
            camouflageEntity.setMaster(player);
            camouflageEntity.hidePlayer(player);
            camouflageEntity.spawnToAll();

            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }

/*        LinkedList<Position> positions = new LinkedList<>();
        for (Position position : this.getRandomSpawn()) {
            int count = Tools.rand(10, 20);
            for (int c = 0; c < count; c++) {
                positions.add(position.add(Tools.rand(-30, 30), position.getFloorY(), Tools.rand(-30, 30)));
            }
        }
        for (Position position : positions) {
            //检查地面
            for (int y = position.getFloorY() + 5; y > 0; y--) {
                if (!position.getLevelBlock().canPassThrough()) {
                    break;
                }
                position.setY(y);
            }
            if (position.getFloorY() == 0) {
                continue;
            }
            position.y += 1;
            EntityCamouflageEntity.create(position.getChunk(),
                    Entity.getDefaultNBT(position),
                    EntityData.getRandomEntityName()
            ).spawnToAll();
        }*/

        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.huntGame,
                new AnimalSpawnTask(this.huntGame, this),
                25, true
        );
    }

    @Override
    public synchronized void endGame(int victory) {
        for (EntityCamouflageEntity entity : this.playerCamouflageEntity.values()) {
            if (entity != null && !entity.isClosed()) {
                entity.setMaster(null);
                entity.close();
            }
        }

        super.endGame(victory);
    }

    @Override
    public void timeTask() {
        super.timeTask();

        int count = 0;
        while (!this.animalSpawnList.isEmpty()) {
            if (count > 100) {
                break;
            }
            count++;
            Position first = this.animalSpawnList.poll();
            if (first != null) {
                EntityCamouflageEntity.create(first.getChunk(),
                        Entity.getDefaultNBT(first),
                        EntityData.getRandomEntityName()
                ).spawnToAll();
            }else {
                break;
            }
        }

        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            final Language language = this.huntGame.getLanguage(entry.getKey());
            if (entry.getValue() == 1) {
                entry.getKey().sendTip(this.huntGame.getLanguage().translateString("tip-currentlyDisguisedAnimal",
                        this.playerCamouflageEntity.get(entry.getKey()).getEntityName()));
            }
            LinkedList<String> ms = new LinkedList<>();
            for (String string : language.translateString("gameTimeScoreBoard").split("\n")) {
                ms.add(string.replace("%mode%", Tools.getShowIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.huntGame.getScoreboard().showScoreboard(entry.getKey(), language.translateString("scoreBoardTitle"), ms);
        }
    }

}
