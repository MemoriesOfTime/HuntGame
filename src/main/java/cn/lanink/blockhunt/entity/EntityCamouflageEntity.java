package cn.lanink.blockhunt.entity;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.data.EntityData;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

/**
 * 玩家伪装的动物实体
 *
 * @author LT_Name
 */
public class EntityCamouflageEntity extends EntityLiving {

    @Setter
    @Getter
    private Player master;

    private final HashSet<Player> hiddenPlayers = new HashSet<>();

    private double mx;
    private double mz;
    private int moveTime;

    public static EntityCamouflageEntity create(FullChunk chunk, CompoundTag nbt, String entityName) {
        return create(chunk, nbt, EntityData.getEntityDataByName(entityName));
    }

    public static EntityCamouflageEntity create(FullChunk chunk, CompoundTag nbt, EntityData entityData) {
        return new EntityCamouflageEntity(chunk, nbt, true) {
            @Override
            public int getNetworkId() {
                return entityData.getNetworkID();
            }

            @Override
            public float getWidth() {
                return entityData.getWidth();
            }

            @Override
            public float getHeight() {
                return entityData.getHeight();
            }
        };
    }

    private EntityCamouflageEntity(FullChunk chunk, CompoundTag nbt, boolean i) {
        super(chunk, nbt);
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setHealth(20f);
        this.namedTag.putBoolean("isBlockHuntEntity", true);
        this.pitch = 0;
    }

    @Deprecated
    public EntityCamouflageEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.close();
    }

    @Override
    public int getNetworkId() {
        return -1;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (this.getMaster() != null) {
            double dx = this.x - this.getMaster().getX();
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
                if (this.moveTime <= 0) {
                    double targetX = BlockHunt.RANDOM.nextDouble() * (BlockHunt.RANDOM.nextBoolean() ? 1 : -1);
                    double targetZ = BlockHunt.RANDOM.nextDouble() * (BlockHunt.RANDOM.nextBoolean() ? 1 : -1);
                    Vector3 target = this.add(targetX, 0, targetZ);

                    double x = target.x - this.x;
                    double z = target.z - this.z;
                    double diff = Math.abs(x) + Math.abs(z);

                    this.mx = 0.15 * (x / diff);
                    this.mz = 0.15 * (z / diff);
                    this.moveTime = Tools.rand(40, 200);

                    double dx = this.x - target.x;
                    double dz = this.z - target.z;
                    double yaw = Math.asin(dx / Math.sqrt(dx * dx + dz * dz)) / 3.14D * 180.0D;
                    if (dz > 0.0D) {
                        yaw = -yaw + 180.0D;
                    }
                    this.yaw = yaw;
                }
            }else if (BlockHunt.RANDOM.nextInt(100) < 10) {
                this.yaw += BlockHunt.RANDOM.nextInt(180) - 60;
            }
        }

        if (this.moveTime > 0) {
            this.moveTime--;
            //TODO 跳跃检查
            Position target = this.add(this.mx, 2, this.mz);
            while (target.getLevelBlock().canPassThrough()) {
                target.y -= 1;
            }
            this.move(this.mx, target.y- this.y, this.mz);
        }

        this.pitch = 0;
        return super.onUpdate(currentTick);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        if (this.getMaster() != null) {
            this.getMaster().setHealth(Math.max(health, 1.5f));
        }
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
        if (this.getMaster() != null) {
            this.getMaster().knockBack(attacker, damage, x, z, base);
        }else {
            super.knockBack(attacker, damage, x, z, base);
        }
    }

    @Override
    public void spawnTo(Player player) {
        if (this.canSee(player)) {
            super.spawnTo(player);
        }
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
