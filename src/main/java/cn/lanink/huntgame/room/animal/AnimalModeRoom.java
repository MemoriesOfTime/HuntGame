package cn.lanink.huntgame.room.animal;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.tasks.game.AnimalSpawnTask;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.potion.Effect;
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

            player.getInventory().setItem(0, Tools.getHuntGameItem(3, player));
            player.getInventory().setItem(4, Tools.getHuntGameItem(21, player));
            player.getInventory().setItem(5, Tools.getHuntGameItem(22, player));
            player.getInventory().setItem(6, Tools.getHuntGameItem(23, player));
            player.getInventory().setItem(7, Tools.getHuntGameItem(24, player));

            player.setScale(1);

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
    public void assignIdentity() {
        if (HuntGame.debug) {
            Player ltname = Server.getInstance().getPlayer("ltname");
            if (ltname != null && this.players.containsKey(ltname)) {
                for (Player player : this.getPlayers().keySet()) {
                    player.getInventory().clearAll();
                    player.getUIInventory().clearAll();
                    if (player == ltname) {
                        this.players.put(player, 2);
                        player.sendTitle(this.huntGame.getLanguage(player).translateString("titleHuntersTitle"),
                                this.huntGame.getLanguage(player).translateString("titleHuntersSubtitle"), 10, 40, 10);
                        continue;
                    }
                    this.players.put(player, 1);
                    player.sendTitle(this.huntGame.getLanguage(player).translateString("titlePreyTitle"),
                            this.huntGame.getLanguage(player).translateString("titlePreySubtitle"), 10, 40, 10);
                }
                return;
            }
        }

        super.assignIdentity();
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
            //嘲讽道具
            PlayerInventory inventory = entry.getKey().getInventory();
            Item coolingItem = Tools.getHuntGameItem(20, entry.getKey());
            Item item = inventory.getItem(4);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() -1);
                }else {
                    item = Tools.getHuntGameItem(21, entry.getKey());
                }
                inventory.setItem(4, item);
            }

            item = inventory.getItem(5);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() -1);
                }else {
                    item = Tools.getHuntGameItem(22, entry.getKey());
                }
                inventory.setItem(5, item);
            }

            item = inventory.getItem(6);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() -1);
                }else {
                    item = Tools.getHuntGameItem(23, entry.getKey());
                }
                inventory.setItem(6, item);
            }

            item = inventory.getItem(7);
            if (coolingItem.equals(item)) {
                if (item.getCount() > 1) {
                    item.setCount(item.getCount() -1);
                }else {
                    item = Tools.getHuntGameItem(24, entry.getKey());
                }
                inventory.setItem(7, item);
            }


            if (this.gameTime < (this.getSetGameTime() - 60) && this.gameTime%10 == 0) {
                if (entry.getValue() == 2 || entry.getValue() == 12) {
                    entry.getKey().addEffect(Effect.getEffect(1).setDuration(400).setVisible(false)); //速度提升 1
                }else if (entry.getValue() == 1) {
                    entry.getKey().addEffect(Effect.getEffect(2).setDuration(400).setVisible(false)); //缓慢1
                }
            }

            entry.getKey().setNameTag("");
            LinkedList<String> ms = new LinkedList<>();
            for (String string : this.huntGame.getLanguage(entry.getKey()).translateString("gameTimeScoreBoard").split("\n")) {
                ms.add(string.replace("%mode%", Tools.getStringIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.huntGame.getScoreboard().showScoreboard(entry.getKey(), this.huntGame.getLanguage(entry.getKey()).translateString("scoreBoardTitle"), ms);
        }
    }

}
