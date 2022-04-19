package cn.lanink.huntgame.room.block;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageBlock;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.PlayerIdentity;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.util.*;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class BlockModeRoom extends BaseRoom {

    @Getter
    protected final HashMap<Player, BlockInfo> playerCamouflageBlock = new HashMap<>();
    @Getter
    protected final HashMap<Player, EntityCamouflageBlock> entityCamouflageBlocks = new HashMap<>();

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public BlockModeRoom(Config config) {
        super(config);
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
    public synchronized void quitRoom(Player player, boolean initiative) {
        super.quitRoom(player, initiative);
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
        int c = 0;
        for (Player player : this.getPlayers().keySet()) {
            if (this.getPlayer(player) == PlayerIdentity.HUNTER) {
                continue;
            }
            if (c >= this.getRandomSpawn().size()) {
                c = 0;
            }
            player.teleport(this.getRandomSpawn().get(c));
            c++;
            player.setScale(0.5F);

            BlockInfo blockInfo = null;
            for (int x = -3; x < 3; x++) {
                for (int y = -3; y < 3; y++) {
                    for (int z = -3; z < 3; z++) {
                        if (blockInfo != null) {
                            break;
                        }
                        Block block = player.add(x, y, z).getLevelBlock();
                        if (block.isNormalBlock()) {
                            blockInfo = new BlockInfo(block.getId(), block.getDamage());
                        }
                    }
                }
            }
            if (blockInfo == null) {
                blockInfo = new BlockInfo(2, 0);
            }
            this.playerCamouflageBlock.put(player, blockInfo);

            player.getInventory().setItem(0, Tools.getHuntGameItem(3, player));
            Item block = Item.get(blockInfo.getId(), blockInfo.getDamage());
            block.setCustomName(HuntGame.getInstance().getLanguage(player).translateString("item-name-currentlyDisguisedBlock"));
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
            for (Map.Entry<Player, PlayerIdentity> entry : this.players.entrySet()) {
                if (entry.getValue() != PlayerIdentity.PREY) {
                    continue;
                }
                Set<Player> p = new HashSet<>(this.players.keySet());
                p.remove(entry.getKey());
                BlockInfo blockInfo = this.getPlayerCamouflageBlock(entry.getKey());
                Block block = Block.get(blockInfo.getId(), blockInfo.getDamage(), entry.getKey().floor());
                entry.getKey().getLevel().sendBlocks(p.toArray(new Player[0]), new Vector3[] { block });
            }
        }

        for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            LinkedList<String> ms = new LinkedList<>();
            for (String string : this.huntGame.getLanguage(entry.getKey()).translateString("gameTimeScoreBoard").split("\n")) {
                ms.add(string.replace("%mode%", Tools.getShowIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.huntGame.getScoreboard().showScoreboard(entry.getKey(), this.huntGame.getLanguage(entry.getKey()).translateString("scoreBoardTitle"), ms);
        }
    }

    /**
     * 获取玩家伪装的方块
     * @param player 玩家
     * @return 方块id
     */
    public BlockInfo getPlayerCamouflageBlock(Player player) {
        return this.playerCamouflageBlock.get(player);
    }

    public EntityCamouflageBlock getEntityCamouflageBlocks(Player player) {
        return this.entityCamouflageBlocks.get(player);
    }

}
