package cn.lanink.blockhunt.entity;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.SerializedImage;

/**
 * 玩家伪装的方块实体（用于判断攻击）
 *
 * @author lt_name
 */
public class EntityCamouflageBlock extends EntityHuman {

    public static final Skin EMPTY_SKIN = new Skin();

    static {
        EMPTY_SKIN.setSkinData(SerializedImage.fromLegacy(new byte[Skin.DOUBLE_SKIN_SIZE]));
        EMPTY_SKIN.generateSkinId("EntityCamouflageBlock");
    }

    public EntityCamouflageBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setSkin(EMPTY_SKIN);
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setDataFlag(0, 5, true);
        this.namedTag.putBoolean("isBlockHuntEntity", true);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
    }

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

}
