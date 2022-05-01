package cn.lanink.huntgame.entity;

import cn.lanink.huntgame.entity.data.EntityData;
import cn.lanink.huntgame.entity.lib.WalkingEntity;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageEvent;
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
public class EntityCamouflageEntityDamage extends WalkingEntity implements IEntityCamouflage {

    static {
        Entity.registerEntity("EntityCamouflageEntityDamage", EntityCamouflageEntityDamage.class);
    }

    @Getter
    private final String entityName;

    @Setter
    @Getter
    private Player master;

    private final HashSet<Player> hiddenPlayers = new HashSet<>();

    private double mx;
    private double mz;
    private int moveTime;

    public static EntityCamouflageEntityDamage create(FullChunk chunk, CompoundTag nbt, String entityName) {
        EntityData entityData = EntityData.getEntityDataByName(entityName);
        return new EntityCamouflageEntityDamage(chunk, nbt, entityName) {
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

            @Override
            public float getLength() {
                return entityData.getLength();
            }
        };
    }

    @Override
    public int getNetworkId() {
        throw new RuntimeException("错误的实例化 EntityCamouflageEntity");
    }

    @Deprecated
    public EntityCamouflageEntityDamage(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.entityName = "Pig";
        this.close();
    }

    private EntityCamouflageEntityDamage(FullChunk chunk, CompoundTag nbt, String entityName) {
        super(chunk, nbt);
        this.entityName = entityName;
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setHealth(20f);
        this.namedTag.putBoolean("isHuntGameEntity", true);
        this.pitch = 0;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
    }

    @Override
    public void onAttack(EntityDamageEvent paramEntityDamageEvent) {

    }

    @Override
    public void attackEntity(EntityCreature paramEntityCreature) {

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
    public boolean setMotion(Vector3 motion) {
        if (super.setMotion(motion)) {
            this.addMotion(this.motionX, this.motionY, this.motionZ);
            return true;
        } else {
            return false;
        }
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
