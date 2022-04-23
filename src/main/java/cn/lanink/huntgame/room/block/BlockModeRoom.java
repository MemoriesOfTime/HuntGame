package cn.lanink.huntgame.room.block;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageBlock;
import cn.lanink.huntgame.entity.EntityCamouflageBlockDamage;
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
import lombok.NonNull;

import java.util.*;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class BlockModeRoom extends BaseRoom {

    @Getter
    protected final HashMap<Player, BlockInfo> playerCamouflageBlockInfoMap = new HashMap<>();
    @Getter
    protected final HashMap<Player, EntityCamouflageBlockDamage> entityCamouflageBlockDamageMap = new HashMap<>();
    @Getter
    protected final HashMap<Player, EntityCamouflageBlock> entityCamouflageBlockMap = new HashMap<>();

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
        if (this.playerCamouflageBlockInfoMap != null) {
            this.playerCamouflageBlockInfoMap.clear();
        }
        if (this.entityCamouflageBlockDamageMap != null) {
            this.entityCamouflageBlockDamageMap.clear();
        }
        if (this.entityCamouflageBlockMap != null) {
            this.entityCamouflageBlockMap.clear();
        }
    }

    @Override
    public synchronized void quitRoom(Player player, boolean initiative) {
        super.quitRoom(player, initiative);
        this.playerCamouflageBlockInfoMap.remove(player);
        EntityCamouflageBlockDamage camouflageBlock = this.entityCamouflageBlockDamageMap.remove(player);
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
            this.playerCamouflageBlockInfoMap.put(player, blockInfo);

            player.getInventory().setItem(0, Tools.getHuntGameItem(3, player));
            Item block = Item.get(blockInfo.getId(), blockInfo.getDamage());
            block.setCustomName(HuntGame.getInstance().getLanguage(player).translateString("item-name-currentlyDisguisedBlock"));
            player.getInventory().setItem(8, block);

            //伤害判断实体
            CompoundTag tag = Entity.getDefaultNBT(player);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", EntityCamouflageBlockDamage.EMPTY_SKIN.getSkinData().data)
                    .putString("ModelId", EntityCamouflageBlockDamage.EMPTY_SKIN.getSkinId()));
            tag.putFloat("Scale", 1.0F);
            tag.putString("playerName", player.getName());
            EntityCamouflageBlockDamage entity = new EntityCamouflageBlockDamage(player.getChunk(), tag, player);
            entity.hidePlayer(player);
            entity.spawnToAll();
            this.entityCamouflageBlockDamageMap.put(player, entity);

            //显示给猎物自己查看的伪装实体 （其他玩家无需考虑碰撞所以用发包显示普通方块）
            EntityCamouflageBlock entityCamouflageBlock = new EntityCamouflageBlock(player.getChunk(), tag, player, blockInfo);
            entityCamouflageBlock.spawnToAll();
            this.entityCamouflageBlockMap.put(player, entityCamouflageBlock);

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
                BlockInfo blockInfo = this.getPlayerCamouflageBlockInfo(entry.getKey());
                Block block = Block.get(blockInfo.getId(), blockInfo.getDamage(), entry.getKey().floor());
                entry.getKey().getLevel().sendBlocks(p.toArray(new Player[0]), new Vector3[] { block });
                //猎物自己查看的伪装方块无需频繁更新
                if (this.gameTime%10 == 0) {
                    this.getEntityCamouflageBlock(entry.getKey()).respawnToAll();
                }
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
    public BlockInfo getPlayerCamouflageBlockInfo(@NonNull Player player) {
        return this.playerCamouflageBlockInfoMap.get(player);
    }

    public EntityCamouflageBlockDamage getEntityCamouflageBlockDamage(@NonNull Player player) {
        return this.entityCamouflageBlockDamageMap.get(player);
    }

    public EntityCamouflageBlock getEntityCamouflageBlock(@NonNull Player player) {
        return this.entityCamouflageBlockMap.get(player);
    }

}
