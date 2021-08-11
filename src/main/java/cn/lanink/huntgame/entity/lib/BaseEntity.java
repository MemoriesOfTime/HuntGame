package cn.lanink.huntgame.entity.lib;

import cn.lanink.gamecore.api.Info;
import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

@Info("https://github.com/Nukkit-coders/MobPlugin/blob/master/src/main/java/nukkitcoders/mobplugin/entities/BaseEntity.java")
public abstract class BaseEntity extends EntityCreature {
    protected int healTime = 0;

    int stayTime = 0;

    int moveTime = 0;

    Vector3 target = null;

    EntityCreature followTarget = null;

    private boolean movement = true;

    private boolean friendly = false;

    protected int attackDelay = 0;

    protected int damageDelay = 0;

    boolean canAttack = true;

    public int attackSleepTime = 23;

    public float speed = 1.0F;

    public BaseEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        if (this.namedTag.contains("Movement"))
            setMovement(this.namedTag.getBoolean("Movement"));
        if (this.namedTag.contains("Age"))
            this.age = this.namedTag.getShort("Age");
    }

    public boolean hasNoTarget() {
        return (getFollowTarget() == null || (getFollowTarget() != null && targetOption(getFollowTarget(), distanceSquared(getFollowTarget()))));
    }

    public int getAttackSleepTime() {
        return this.attackSleepTime;
    }

    public boolean isFriendly() {
        return this.friendly;
    }

    public boolean isMovement() {
        return this.movement;
    }

    public boolean isKnockback() {
        return (this.attackTime > 0);
    }

    public void setFriendly(boolean bool) {
        this.friendly = bool;
    }

    public void setMovement(boolean value) {
        this.movement = value;
    }

    public double getSpeed() {
        return this.speed;
    }

    public Vector3 getTarget() {
        return this.target;
    }

    public void setTarget(Vector3 vec) {
        this.target = vec;
    }

    public EntityCreature getFollowTarget() {
        return (this.followTarget != null) ? this.followTarget : ((this.target instanceof EntityCreature) ? (EntityCreature)this.target : null);
    }

    public void setFollowTarget(EntityCreature target) {
        this.followTarget = target;
        this.moveTime = 0;
        this.stayTime = 0;
        this.target = null;
    }

    protected boolean isPlayerTarget(Player player) {
        return (!player.closed && player.isAlive() && (player.isSurvival() || player.isAdventure()));
    }

    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player)creature;
            return (player.closed || !player.spawned || !player.isAlive() || (!player.isSurvival() && !player.isAdventure()) || distance > 80.0D ||
                    player.getLevel() != getLevel());
        }
        return (creature.closed || !creature.isAlive() || !creature.getLevel().getFolderName().equalsIgnoreCase(getLevel().getFolderName()));
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        super.entityBaseTick(tickDiff);
        if (!isAlive())
            close();
        if (this.attackDelay < 1000)
            this.attackDelay++;
        if (this.damageDelay < 1000)
            this.damageDelay++;
        if (this.healTime < 1000)
            this.healTime++;
        return true;
    }

    public abstract void onAttack(EntityDamageEvent paramEntityDamageEvent);

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (isKnockback() && source instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)source).getDamager() instanceof Player)
            return false;
        if (this.fireProof && (source.getCause() == EntityDamageEvent.DamageCause.FIRE || source
                .getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || source
                .getCause() == EntityDamageEvent.DamageCause.LAVA))
            return false;
        if (source instanceof EntityDamageByEntityEvent) {
            ((EntityDamageByEntityEvent)source).setKnockBack(0.3F);
            onAttack(source);
        }
        this.target = null;
        this.stayTime = 0;
        super.attack(source);
        return true;
    }

    public abstract void attackEntity(EntityCreature paramEntityCreature);

    @Override
    public void saveNBT() {

    }

}