package cn.lanink.blockhunt.entity;

import cn.lanink.blockhunt.BlockHunt;
import cn.nukkit.Player;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

/**
 * @author LT_Name
 */
public class EntityCamouflageEntity extends EntityLiving {

    @Setter
    private int networkID;

    @Setter
    @Getter
    private Player master;

    private final HashSet<Player> hiddenPlayers = new HashSet<>();

    @Deprecated
    public EntityCamouflageEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.close();
    }

    public EntityCamouflageEntity(FullChunk chunk, CompoundTag nbt, int networkID) {
        super(chunk, nbt);
        this.networkID = networkID;
    }

    @Override
    public int getNetworkId() {
        return this.networkID;
    }

    public float getWidth() {
        return 0.9F;
    }

    public float getHeight() {
        return 0.9F;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.getMaster() != null) {
            double dx = this.x - this.getMaster().getX();
            double dy = this.y - this.getMaster().getY();
            double dz = this.z - this.getMaster().getZ();
            double yaw = Math.asin(dx / Math.sqrt(dx * dx + dz * dz)) / 3.14D * 180.0D;
            if (dz > 0.0D) {
                yaw = -yaw + 180.0D;
            }
            this.yaw = yaw;

            this.x = this.getMaster().getX();
            this.y = this.getMaster().getY();
            this.z = this.getMaster().getZ();
        }else if (currentTick%60 == 0) {
            //进行一些随机移动
            if (BlockHunt.RANDOM.nextInt(100) < 10) {

            }else if (BlockHunt.RANDOM.nextInt(100) < 30) {
                this.yaw += BlockHunt.RANDOM.nextInt(180) - 60;
            }
        }
        this.pitch = 0;
        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnTo(Player player) {
        if (this.canSee(player)) {
            super.spawnTo(player);
        }
    }

    public boolean canSee(Player player) {
        return !this.hiddenPlayers.contains(player);
    }

    public void hidePlayer(Player player) {
        this.hiddenPlayers.add(player);
        this.despawnFrom(player);
    }

    public void showPlayer(Player player) {
        this.hiddenPlayers.remove(player);
        if (player.isOnline()) {
            this.spawnTo(player);
        }
    }

}
