package cn.lanink.huntgame.entity;

import cn.lanink.huntgame.room.block.BlockInfo;
import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author LT_Name
 */
public class EntityCamouflageBlock extends EntityCreature implements IEntityCamouflage {

    public static final int NETWORK_ID = 66;

    @Setter
    @Getter
    private Player master;

    @Getter
    private BlockInfo blockInfo;

    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.001f;
    }

    @Override
    public float getLength() {
        return 0.001f;
    }

    @Override
    public float getHeight() {
        return 0.001f;
    }

    @Override
    protected float getBaseOffset() {
        return 0.49f;
    }

    public EntityCamouflageBlock(FullChunk chunk, CompoundTag nbt, @NonNull Player master, @NonNull BlockInfo blockInfo) {
        super(chunk, nbt);
        this.master = master;
        this.setBlockInfo(blockInfo);
    }

    public void setBlockInfo(@NonNull BlockInfo blockInfo) {
        this.blockInfo = blockInfo;
        this.setDataProperty(new IntEntityData(DATA_VARIANT, GlobalBlockPalette.getOrCreateRuntimeId(this.blockInfo.getId(), this.blockInfo.getDamage())));
        this.respawnToAll();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (this.getMaster() != null) {
            int tickDiff = currentTick - this.lastUpdate;
            if (tickDiff <= 0) {
                return false;
            } else {
                this.lastUpdate = currentTick;
                boolean hasUpdate = this.entityBaseTick(tickDiff);
                this.updateMovement();
                return hasUpdate;
            }
        }
        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnTo(Player player) {
        if (this.canSee(player)) {
            super.spawnTo(player);
        }
    }

    @Override
    public boolean canSee(Player player) {
        return this.getMaster() == null || player == this.getMaster();
    }

    @Override
    public void hidePlayer(Player player) {
        throw new RuntimeException();
    }

    @Override
    public void showPlayer(Player player) {
        throw new RuntimeException();
    }

}
