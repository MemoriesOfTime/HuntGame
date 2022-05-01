package cn.lanink.huntgame.room.animal;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.EntityCamouflageEntityDamage;
import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.PlayerIdentity;
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
    protected final HashMap<Player, EntityCamouflageEntityDamage> playerCamouflageEntityDamageMap = new HashMap<>();
    @Getter
    protected final HashMap<Player, EntityCamouflageEntity> playerCamouflageEntityMap = new HashMap<>();

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
        if (this.playerCamouflageEntityDamageMap != null) {
            this.playerCamouflageEntityDamageMap.clear();
        }
        if (this.playerCamouflageEntityMap != null) {
            this.playerCamouflageEntityMap.clear();
        }
        if (this.animalSpawnList != null) {
            this.animalSpawnList.clear();
        }
    }

    @Override
    public synchronized void quitRoom(Player player, boolean initiative) {
        super.quitRoom(player, initiative);

        EntityCamouflageEntityDamage entityCamouflageEntityDamage = this.playerCamouflageEntityDamageMap.remove(player);
        if (entityCamouflageEntityDamage != null && !entityCamouflageEntityDamage.isClosed()) {
            entityCamouflageEntityDamage.close();
        }
        EntityCamouflageEntity entityCamouflageEntity = this.playerCamouflageEntityMap.remove(player);
        if (entityCamouflageEntity != null && !entityCamouflageEntity.isClosed()) {
            entityCamouflageEntity.close();
        }
    }

    @Override
    public void gameStart() {
        super.gameStart();
        int x = 0;
        for (Player player: this.players.keySet()) {
            if (this.getPlayer(player) != PlayerIdentity.PREY) {
                continue;
            }
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;

            String randomEntityName = EntityData.getRandomEntityName();
            EntityCamouflageEntityDamage camouflageEntity = EntityCamouflageEntityDamage.create(player.chunk, Entity.getDefaultNBT(player), randomEntityName);
            this.playerCamouflageEntityDamageMap.put(player, camouflageEntity);
            camouflageEntity.setMaster(player);
            camouflageEntity.hidePlayer(player);
            camouflageEntity.spawnToAll();

            EntityCamouflageEntity entityCamouflageEntityTest = EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), randomEntityName);
            entityCamouflageEntityTest.setMaster(player);
            entityCamouflageEntityTest.spawnToAll();
            this.playerCamouflageEntityMap.put(player, entityCamouflageEntityTest);

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
    public synchronized void endGame(PlayerIdentity victory) {
        for (EntityCamouflageEntityDamage entity : this.playerCamouflageEntityDamageMap.values()) {
            if (entity != null && !entity.isClosed()) {
                entity.close();
            }
        }
        for (EntityCamouflageEntity entity : this.playerCamouflageEntityMap.values()) {
            if (entity != null && !entity.isClosed()) {
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
                EntityCamouflageEntityDamage.create(first.getChunk(),
                        Entity.getDefaultNBT(first),
                        EntityData.getRandomEntityName()
                ).spawnToAll();
            }else {
                break;
            }
        }

        for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            final Language language = this.huntGame.getLanguage(entry.getKey());
            if (entry.getValue() == PlayerIdentity.PREY) {
                entry.getKey().sendTip(language.translateString("tip-currentlyDisguisedAnimal",
                        language.translateString("animal-name-" + this.playerCamouflageEntityDamageMap.get(entry.getKey()).getEntityName())));
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
