package cn.lanink.huntgame.room.animal;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
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

            String randomEntityName = EntityData.getRandomEntityName();
            EntityCamouflageEntity camouflageEntity =
                    EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), randomEntityName);
            this.playerCamouflageEntity.put(player, camouflageEntity);
            camouflageEntity.setMaster(player);
            camouflageEntity.hidePlayer(player);
            camouflageEntity.spawnToAll();

            Item item = Item.get(280);
            item.setCustomName("伪装道具\n更换伪装：点击要伪装的生物");
            player.getInventory().setItem(0, item);

            player.setScale(1);

            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }

        //TODO 改为在地图上随机生成实体
        LinkedList<Position> positions = new LinkedList<>();
        for (Position position : this.getRandomSpawn()) {
            int count = Tools.rand(15, 30);
            for (int c = 0; c < count; c++) {
                positions.add(position.add(Tools.rand(-50, 50), position.getFloorY(), Tools.rand(-50, 50)));
            }
        }
        for (Position position : positions) {
            //检查地面
            for (int y = position.getFloorY() + 10; y > 0; y--) {
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
        }
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
                        player.sendTitle(this.huntGame.getLanguage(player).titleHuntersTitle,
                                this.huntGame.getLanguage(player).titleHuntersSubtitle, 10, 40, 10);
                        continue;
                    }
                    this.players.put(player, 1);
                    player.sendTitle(this.huntGame.getLanguage(player).titlePreyTitle,
                            this.huntGame.getLanguage(player).titlePreySubtitle, 10, 40, 10);
                }
                return;
            }
        }

        super.assignIdentity();
    }

    @Override
    public void timeTask() {
        super.timeTask();

        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            LinkedList<String> ms = new LinkedList<>();
            for (String string : this.huntGame.getLanguage(entry.getKey()).gameTimeScoreBoard.split("\n")) {
                ms.add(string.replace("%mode%", Tools.getStringIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.huntGame.getScoreboard().showScoreboard(entry.getKey(), this.huntGame.getLanguage(entry.getKey()).scoreBoardTitle, ms);
        }
    }

}
