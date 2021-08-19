package cn.lanink.huntgame.entity.lib;

import cn.lanink.gamecore.api.Info;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

@Info("https://github.com/Nukkit-coders/MobPlugin/blob/master/src/main/java/nukkitcoders/mobplugin/entities/WalkingEntity.java")
public abstract class WalkingEntity extends BaseEntity {

    public WalkingEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private boolean checkJump(double dx, double dz) {
        if (this.motionY == (getGravity() * 2.0F))
            return this.level.getBlock(new Vector3(NukkitMath.floorDouble(this.x), (int)this.y, NukkitMath.floorDouble(this.z))) instanceof cn.nukkit.block.BlockLiquid;
        if (this.level.getBlock(new Vector3(NukkitMath.floorDouble(this.x), (int)(this.y + 0.8D), NukkitMath.floorDouble(this.z))) instanceof cn.nukkit.block.BlockLiquid) {
            this.motionY = (getGravity() * 2.0F);
            return true;
        }
        if (this.onGround && this.stayTime <= 0) {
            Block that = getLevel().getBlock(new Vector3(NukkitMath.floorDouble(this.x + dx), (int)this.y, NukkitMath.floorDouble(this.z + dz)));
            if (getDirection() == null)
                return false;
            Block block = that.getSide(getHorizontalFacing());
            if (!block.canPassThrough() && block.up().canPassThrough() && that.up(2).canPassThrough()) {
                if (!(block instanceof cn.nukkit.block.BlockFence) && !(block instanceof cn.nukkit.block.BlockFenceGate)) {
                    if (this.motionY <= (getGravity() * 4.0F)) {
                        this.motionY = (getGravity() * 4.0F);
                    } else if (block instanceof cn.nukkit.block.BlockStairs) {
                        this.motionY = (getGravity() * 4.0F);
                    } else if (this.motionY <= (getGravity() * 8.0F)) {
                        this.motionY = (getGravity() * 8.0F);
                    } else {
                        this.motionY += getGravity() * 0.25D;
                    }
                } else {
                    this.motionY = getGravity();
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void setFollowTarget(EntityCreature target) {
        setFollowTarget(target, true);
    }

    public void setFollowTarget(EntityCreature target, boolean attack) {
        super.setFollowTarget(target);
        this.canAttack = attack;
    }

    private void checkTarget() {
        if (!isKnockback() &&
                (this.followTarget == null || this.followTarget.closed || !this.followTarget.isAlive() ||
                        targetOption(this.followTarget, distanceSquared(this.followTarget)) || this.target == null)) {
            double near = 2.147483647E9D;
            if (this.followTarget == null) {
                if (this.passengers.isEmpty())
                    for (Entity entity : getLevel().getEntities()) {
                        if (entity instanceof EntityCreature &&
                                entity != this)
                            if (!(entity instanceof Player)) {
                                near = getAttackChunk(near, entity);
                            }
                    }
                if (this.followTarget == null || this.followTarget.closed || !this.followTarget.isAlive() || targetOption(this.followTarget,
                        distanceSquared(this.followTarget)) || this.target == null)
                    if (this.stayTime > 0) {
                        if (Tools.rand(1, 1000) > 50)
                            return;
                        int x = Tools.rand(10, 30);
                        int z = Tools.rand(10, 30);
                        this.target = add((Tools.rand(1, 100) <= 40) ? x : -x, Tools.rand(-20.0D, 20.0D) / 10.0D, (Tools.rand(1, 100) <= 60) ? z : -z);
                    } else if (Tools.rand(1, 1000) <= 10) {
                        int x = Tools.rand(10, 30);
                        int z = Tools.rand(10, 30);
                        this.stayTime = Tools.rand(200, 600);
                        this.target = add((Tools.rand(1, 100) <= 40) ? x : -x, Tools.rand(-20.0D, 20.0D) / 10.0D, (Tools.rand(1, 100) <= 60) ? z : -z);
                    } else if ((this.moveTime <= 0 || this.target == null) &&
                            Tools.rand(1, 1000) <= 70) {
                        int x = Tools.rand(20, 100);
                        int z = Tools.rand(20, 100);
                        this.stayTime = 0;
                        this.moveTime = Tools.rand(30, 200);
                        this.target = add((Tools.rand(1, 100) <= 40) ? x : -x, 0.0D, (Tools.rand(1, 100) <= 60) ? z : -z);
                    }
            } else {
                double distance = distanceSquared(this.followTarget);
                if (distance > near || targetOption(this.followTarget, distance)) {
                    setFollowTarget(null, false);
                    return;
                }
                if (this.target == null) {
                    this.stayTime = 0;
                    this.moveTime = 0;
                    if (this.passengers.isEmpty())
                        this.target = this.followTarget;
                }
            }
        }
    }

    private double getAttackChunk(double near, Entity entity) {
        if (!(entity instanceof Player))
            return near;
        near = getFightEntity(near, (EntityCreature)entity);
        return near;
    }

    private double getFightEntity(double near, EntityCreature entity) {
        double distance = distanceSquared(entity);
        if (distance > near || targetOption(entity, distance))
            return near;
        near = distance;
        this.stayTime = 0;
        this.moveTime = 0;
        this.followTarget = entity;
        if (this.passengers.isEmpty())
            this.target = entity;
        this.canAttack = true;
        return near;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed)
            return false;
        if (!isAlive()) {
            if (++this.deadTicks >= 23) {
                close();
                return false;
            }
            return true;
        }
        int tickDiff = currentTick - this.lastUpdate;
        this.lastUpdate = currentTick;
        entityBaseTick(tickDiff);
        Vector3 target = updateMove(tickDiff);
        if (target instanceof EntityCreature) {
            if (targetOption((EntityCreature)target, distanceSquared(target))) {
                setFollowTarget(null, false);
                return true;
            }
            if (target instanceof Player) {
                if (isPlayerTarget((Player)target) && (
                        target != this.followTarget || this.canAttack))
                    if (!targetOption((EntityCreature)target, distanceSquared(target))) {
                        attackEntity((EntityCreature)target);
                    } else {
                        setFollowTarget(null, false);
                    }
            } else if (!targetOption((EntityCreature)target, distanceSquared(target))) {
                attackEntity((EntityCreature)target);
            } else {
                setFollowTarget(null, false);
            }
        } else if (target != null && Math.pow(this.x - target.x, 2.0D) + Math.pow(this.z - target.z, 2.0D) <= 1.0D) {
            this.moveTime = 0;
        }
        return true;
    }

    private Vector3 updateMove(int tickDiff) {
        if (!isImmobile()) {
            if (!isMovement())
                return null;
            if (isKnockback()) {
                move(this.motionX, this.motionY, this.motionZ);
                this.motionY -= getGravity();
                updateMovement();
                return null;
            }
            if (this.followTarget != null && !this.followTarget.closed && this.followTarget.isAlive() && this.target != null) {
                double d1 = this.target.x - this.x;
                double d2 = this.target.z - this.z;
                double diff = Math.abs(d1) + Math.abs(d2);
                if (diff <= 0.0D)
                    diff = 0.1D;
                if (this.stayTime <= 0 && distance(this.followTarget) > (getWidth() + 0.0D) / 2.0D + 0.05D) {
                    if (isInsideOfWater()) {
                        this.motionX = getSpeed() * 0.05D * d1 / diff;
                        this.motionZ = getSpeed() * 0.05D * d2 / diff;
                        this.level.addParticle(new BubbleParticle(add(Tools.rand(-2.0D, 2.0D), Tools.rand(-0.5D, 0.0D), Tools.rand(-2.0D, 2.0D))));
                    } else {
                        this.motionX = getSpeed() * 0.1D * d1 / diff;
                        this.motionZ = getSpeed() * 0.1D * d2 / diff;
                    }
                } else {
                    this.motionX = 0.0D;
                    this.motionZ = 0.0D;
                }
                if (this.passengers.isEmpty() && (this.stayTime <= 0 || Tools.rand(0, 1000) < 10)) {
                    this.yaw = Math.toDegrees(-Math.atan2(d1 / diff, d2 / diff));
                }
            }
            Vector3 before = this.target;
            checkTarget();
            if ((this.target != null || before != this.target) &&
                    this.target != null) {
                double d1 = this.target.x - this.x;
                double d2 = this.target.z - this.z;
                double diff = Math.abs(d1) + Math.abs(d2);
                if (diff <= 0.0D)
                    diff = 0.1D;
                if (this.stayTime <= 0 && distance(this.target) > (getWidth() + 0.0D) / 2.0D + 0.05D) {
                    if (isInsideOfWater()) {
                        this.motionX = getSpeed() * 0.05D * d1 / diff;
                        this.motionZ = getSpeed() * 0.05D * d2 / diff;
                        this.level.addParticle(new BubbleParticle(add(Tools.rand(-2.0D, 2.0D), Tools.rand(-0.5D, 0.0D), Tools.rand(-2.0D, 2.0D))));
                    } else {
                        this.motionX = getSpeed() * 0.15D * d1 / diff;
                        this.motionZ = getSpeed() * 0.15D * d2 / diff;
                    }
                } else {
                    this.motionX = 0.0D;
                    this.motionZ = 0.0D;
                }
                if (this.passengers.isEmpty() && (this.stayTime <= 0 || Tools.rand(0, 1000) < 10)) {
                    this.yaw = Math.toDegrees(-Math.atan2(d1 / diff, d2 / diff));
                }
            }
            double x = this.motionX * tickDiff;
            double z = this.motionZ * tickDiff;
            boolean isJump = checkJump(x, z);
            if (this.stayTime > 0) {
                this.stayTime -= tickDiff;
                move(0.0D, this.motionY, 0.0D);
            } else {
                Vector2 be = new Vector2(this.x + x, this.z + z);
                move(x, this.motionY, z);
                Vector2 af = new Vector2(this.x, this.z);
                if ((be.x != af.x || be.y != af.y) && !isJump)
                    this.moveTime -= 90 * tickDiff;
            }
            if (!isJump)
                if (this.onGround) {
                    this.motionY = 0.0D;
                } else if (this.motionY > (-getGravity() * 4.0F)) {
                    if (!(this.level.getBlock(new Vector3(NukkitMath.floorDouble(this.x), (int)(this.y + 0.8D), NukkitMath.floorDouble(this.z))) instanceof cn.nukkit.block.BlockLiquid))
                        this.motionY -= (getGravity());
                } else {
                    this.motionY -= (getGravity() * tickDiff);
                }
            updateMovement();
            return (this.followTarget != null) ? this.followTarget : this.target;
        }
        return null;
    }
}
