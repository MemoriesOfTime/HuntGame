package cn.lanink.huntgame.room.animal;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.EntityCamouflageEntityDamage;
import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.PlayerData;
import cn.lanink.huntgame.room.PlayerIdentity;
import cn.lanink.huntgame.tasks.game.AnimalSpawnTask;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author LT_Name
 */
public class AnimalModeRoom extends BaseRoom {

    //每秒最多生成的实体数量
    private static final int MAX_SPAWN_ENTITY = 100;

    @Getter
    protected final HashMap<Player, EntityCamouflageEntityDamage> playerCamouflageEntityDamageMap = new HashMap<>();
    @Getter
    protected final HashMap<Player, EntityCamouflageEntity> playerCamouflageEntityMap = new HashMap<>();

    @Getter
    private final ConcurrentLinkedQueue<Position> animalSpawnList = new ConcurrentLinkedQueue<>();

    /**
     * 初始化
     *
     * @param level 游戏世界
     * @param config 配置文件
     */
    public AnimalModeRoom(@NotNull Level level, @NotNull Config config) {
        super(level, config);
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
            for (EntityCamouflageEntityDamage entity : this.playerCamouflageEntityDamageMap.values()) {
                if (entity != null && !entity.isClosed()) {
                    entity.close();
                }
            }
            this.playerCamouflageEntityDamageMap.clear();
        }
        if (this.playerCamouflageEntityMap != null) {
            for (EntityCamouflageEntity entity : this.playerCamouflageEntityMap.values()) {
                if (entity != null && !entity.isClosed()) {
                    entity.close();
                }
            }
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
            if (this.getPlayer(player).getIdentity() != PlayerIdentity.PREY) {
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

            EntityCamouflageEntity entityCamouflageEntity = EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), randomEntityName);
            entityCamouflageEntity.setMaster(player);
            entityCamouflageEntity.spawnToAll();
            this.playerCamouflageEntityMap.put(player, entityCamouflageEntity);

            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }

        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.huntGame,
                new AnimalSpawnTask(this.huntGame, this),
                25, true
        );
    }

    @Override
    public void timeTask() {
        super.timeTask();

        int count = 0;
        while (!this.animalSpawnList.isEmpty()) {
            if (count > MAX_SPAWN_ENTITY) {
                break;
            }
            count++;
            Position position = this.animalSpawnList.poll();
            if (position == null) {
                break;
            }
            EntityCamouflageEntityDamage.create(
                    position.getChunk(),
                    Entity.getDefaultNBT(position),
                    EntityData.getRandomEntityName()
            ).spawnToAll();
        }

        for (Map.Entry<Player, PlayerData> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            final Language language = this.huntGame.getLanguage(entry.getKey());
            if (entry.getValue().getIdentity() == PlayerIdentity.PREY) {
                entry.getKey().sendTip(language.translateString("tip-currentlyDisguisedAnimal",
                        language.translateString("animal-name-" + this.playerCamouflageEntityDamageMap.get(entry.getKey()).getEntityName())));
            }
            LinkedList<String> ms = new LinkedList<>();
            for (String string : language.translateString("gameTimeScoreBoard").split("\n")) {
                ms.add(string.replace("%mode%", Tools.getShowIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", Tools.formatCountdown(this.gameTime)));
            }
            this.huntGame.getScoreboard().showScoreboard(entry.getKey(), language.translateString("scoreBoardTitle"), ms);
        }
    }

    @Override
    public void playerDeath(Player player) {
        super.playerDeath(player);
        EntityCamouflageEntity camouflageEntity = this.playerCamouflageEntityMap.remove(player);
        if (camouflageEntity != null && !camouflageEntity.isClosed()) {
            camouflageEntity.close();
        }
        EntityCamouflageEntityDamage camouflageEntityDamage = this.playerCamouflageEntityDamageMap.remove(player);
        if (camouflageEntityDamage != null && !camouflageEntityDamage.isClosed()) {
            camouflageEntityDamage.close();
        }
    }
}
