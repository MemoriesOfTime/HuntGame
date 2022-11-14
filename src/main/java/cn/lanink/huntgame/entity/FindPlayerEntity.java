package cn.lanink.huntgame.entity;

import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * 猎人用于查找猎物的实体
 * 仅显示给使用的猎人
 *
 * @author LT_Name
 */
public class FindPlayerEntity extends Entity {

    private Player player;
    private Player target;

    @Override
    public int getNetworkId() {
        return 76;
    }

    @Deprecated
    public FindPlayerEntity(FullChunk chunk, CompoundTag nbt) {//
        super(chunk, nbt);
    }

    public FindPlayerEntity(Player player, Player target) {
        super(player.chunk, Entity.getDefaultNBT(player.add(0, player.getEyeHeight(), 0)));
        this.player = player;
        this.target = target;
        Vector3 motion = Tools.getMotion(player, target);
        motion.x *= 0.2;
        motion.y *= 0.2;
        motion.z *= 0.2;
        this.setMotion(motion);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.age > 200 || this.distance(this.target) < 2) {
            this.close();
        }

        this.x = this.x + this.motionX;
        this.y = this.y + this.motionY;
        this.z = this.z + this.motionZ;

        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnTo(Player player) {
        if (player == this.player) {
            super.spawnTo(player);
        }
    }

    @Override
    public void spawnToAll() {
        this.spawnTo(this.player);
    }

}
