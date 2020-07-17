package cn.lanink.blockhunt.entity;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class EntityCamouflageBlock extends EntityHuman {

    public EntityCamouflageBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setDataFlag(0, 5, true);
        this.namedTag.putBoolean("isBlockHuntEntity", true);
    }

    @Override
    public int getNetworkId() {
        return 64;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
    }

    @Override
    public float getWidth() {
        return 1.2F;
    }

    @Override
    public float getHeight() {
        return 1.2F;
    }

}
