package cn.lanink.huntgame.room.block;

import cn.lanink.huntgame.entity.EntityCamouflageBlock;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class BlockModeRoom extends BaseRoom {

    protected final ArrayList<String> camouflageBlocks;

    protected final HashMap<Player, Integer[]> playerCamouflageBlock = new HashMap<>();
    protected final HashMap<Player, EntityCamouflageBlock> entityCamouflageBlocks = new HashMap<>();

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public BlockModeRoom(Config config) {
        super(config);
        this.camouflageBlocks = (ArrayList<String>) config.getStringList("blocks");
    }

    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.add("BlockGameListener");
        return list;
    }

    @Override
    public void initData() {
        super.initData();
        if (this.playerCamouflageBlock != null) {
            this.playerCamouflageBlock.clear();
        }
        if (this.entityCamouflageBlocks != null) {
            this.entityCamouflageBlocks.clear();
        }
    }

    @Override
    public synchronized void quitRoom(Player player) {
        super.quitRoom(player);
        this.playerCamouflageBlock.remove(player);
        EntityCamouflageBlock camouflageBlock = this.entityCamouflageBlocks.remove(player);
        if (camouflageBlock != null) {
            camouflageBlock.close();
        }
    }

    /**
     * 房间开始游戏
     */
    @Override
    public synchronized void gameStart() {
        super.gameStart();
        int x = 0;
        for (Player player : this.getPlayers().keySet()) {
            if (this.getPlayers(player) == 2) continue;
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
            player.setScale(0.5F);
            String[] s = this.camouflageBlocks.get(new Random().nextInt(this.camouflageBlocks.size())).split(":");
            Integer[] integers = new Integer[2];
            integers[0] = Integer.parseInt(s[0]);
            integers[1] = Integer.parseInt(s[1]);
            this.playerCamouflageBlock.put(player, integers);

            Item item = Item.get(280);
            item.setCustomName("伪装道具\n更换伪装：点击要伪装的方块");
            player.getInventory().setItem(0, item);
            Item block = Item.get(integers[0], integers[1]);
            block.setCustomName("当前伪装的方块");
            player.getInventory().setItem(8, block);

            CompoundTag tag = Entity.getDefaultNBT(player);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", EntityCamouflageBlock.EMPTY_SKIN.getSkinData().data)
                    .putString("ModelId", EntityCamouflageBlock.EMPTY_SKIN.getSkinId()));
            tag.putFloat("Scale", 1.0F);
            tag.putString("playerName", player.getName());
            EntityCamouflageBlock entity = new EntityCamouflageBlock(player.getChunk(), tag, player);
            entity.hidePlayer(player);
            entity.spawnToAll();
            this.entityCamouflageBlocks.put(player, entity);

            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }
    }

    /**
     * 计时Task
     */
    @Override
    public void timeTask() {
        super.timeTask();

        //防止玩家长时间不动导致方块消失
        if (this.gameTime%5 == 0) {
            for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
                if (entry.getValue() != 1) continue;
                Set<Player> p = new HashSet<>(this.players.keySet());
                p.remove(entry.getKey());
                Integer[] integers = getPlayerCamouflageBlock(entry.getKey());
                Block block = Block.get(integers[0], integers[1], entry.getKey().floor());
                entry.getKey().getLevel().sendBlocks(p.toArray(new Player[0]), new Vector3[] { block });
            }
        }

        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
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

    public HashMap<Player, Integer[]> getPlayerCamouflageBlock() {
        return this.playerCamouflageBlock;
    }

    /**
     * 获取玩家伪装的方块
     * @param player 玩家
     * @return 方块id
     */
    public Integer[] getPlayerCamouflageBlock(Player player) {
        return this.playerCamouflageBlock.get(player);
    }

    public HashMap<Player, EntityCamouflageBlock> getEntityCamouflageBlocks() {
        return this.entityCamouflageBlocks;
    }

    public EntityCamouflageBlock getEntityCamouflageBlocks(Player player) {
        return this.entityCamouflageBlocks.get(player);
    }

}