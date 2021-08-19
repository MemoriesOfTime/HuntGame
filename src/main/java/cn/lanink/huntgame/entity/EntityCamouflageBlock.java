package cn.lanink.huntgame.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.SerializedImage;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

/**
 * 玩家伪装的方块实体（用于判断攻击）
 *
 * @author lt_name
 */
public class EntityCamouflageBlock extends EntityHuman implements IEntityCamouflage {

    public static final Skin EMPTY_SKIN = new Skin();

    static {
        EMPTY_SKIN.setSkinData(SerializedImage.fromLegacy(new byte[Skin.DOUBLE_SKIN_SIZE]));
        EMPTY_SKIN.generateSkinId("EntityCamouflageBlock");
    }

    @Setter
    @Getter
    private Player master;

    private final HashSet<Player> hiddenPlayers = new HashSet<>();

    @Override
    public float getLength() {
        return 1.05F;
    }

    @Override
    public float getWidth() {
        return 1.05F;
    }

    @Override
    public float getHeight() {
        return 1.05F;
    }

    @Deprecated
    public EntityCamouflageBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.close();
    }

    public EntityCamouflageBlock(FullChunk chunk, CompoundTag nbt, Player master) {
        super(chunk, nbt);
        this.master = master;
        this.setSkin(EMPTY_SKIN);
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true); //隐身
        this.namedTag.putBoolean("isHuntGameEntity", true);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (this.getMaster() == null) {
            this.close();
            return false;
        }

        //伪装方块实体坐标必须在方块的中心
        /*Position newPos = this.getMaster().add(0, 0.5, 0).floor().add(0.5, 0, 0.5);
        this.x = newPos.x;
        this.y = newPos.y;
        this.z = newPos.z;*/

        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnTo(Player player) {
        if (this.canSee(player)) {
            super.spawnTo(player);
        }
    }

    @Override
    public void close() {
        this.setMaster(null);
        super.close();
    }

    /**
     * 玩家是否可以看到本实体
     *
     * @param player 玩家
     * @return 玩家是否可以看到本实体
     */
    public boolean canSee(Player player) {
        return !this.hiddenPlayers.contains(player);
    }

    /**
     * 隐藏实体
     *
     * @param player 目标玩家
     */
    public void hidePlayer(Player player) {
        this.hiddenPlayers.add(player);
        this.despawnFrom(player);
    }

    /**
     * 显示实体
     *
     * @param player 目标玩家
     */
    public void showPlayer(Player player) {
        this.hiddenPlayers.remove(player);
        if (player.isOnline()) {
            this.spawnTo(player);
        }
    }

}
