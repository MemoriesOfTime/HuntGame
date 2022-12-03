package cn.lanink.huntgame.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.FindPlayerEntity;
import cn.lanink.huntgame.room.*;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author LT_Name
 */
@SuppressWarnings("unused")
public class DefaultGameListener extends BaseGameListener<BaseRoom> {

    private final HuntGame huntGame = HuntGame.getInstance();

    @EventHandler
    public void onPlayerChangeSkin(PlayerChangeSkinEvent event) { //此事件仅玩家主动修改皮肤时触发，不需要针对插件修改特判
        Player player = event.getPlayer();
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByChildEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = ((Player) event.getEntity());
            BaseRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            Player damager = (Player) event.getDamager();
            if (event.getChild().getNetworkId() == 80 && //箭
                    (room.getPlayer(player).getIdentity() == PlayerIdentity.HUNTER || room.getPlayer(player).getIdentity() == PlayerIdentity.CHANGE_HUNTER)
                    && room.getPlayer(damager).getIdentity() == PlayerIdentity.PREY) {
                event.setDamage(0);
                event.setKnockBack(event.getKnockBack() * 1.5f);
                event.setCancelled(false);

                room.getPlayer(damager).addEventCount(EventType.PREY_BOW_HIT_HUNTER);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        BaseRoom room = this.getListenerRoom(event.getEntity().getLevel());
        if (room == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!room.isPlaying(player)) {
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (room.getStatus() == RoomStatus.GAME) {
                    if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
                        room.playerDeath(player);
                    }else {
                        player.teleport(room.getRandomSpawn().get(Tools.RANDOM.nextInt(room.getRandomSpawn().size())));
                    }
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM && !(event instanceof EntityDamageByChildEntityEvent)) {
                event.setCancelled(true);
            }else if (event.getFinalDamage() + 1 > player.getHealth()) {
                //正常情况下玩家不应该受到其他玩家的伤害（因为他们只能打到伪装方块）
                event.setDamage(0);
                room.playerDeath(player);
            }
        }
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getProjectile() == null) {
                return;
            }
            BaseRoom room = this.getListenerRooms().get(player.getLevel().getName());
            if (room == null || room.getStatus() != RoomStatus.GAME) {
                return;
            }
            event.getProjectile().namedTag.putString("HuntGameDamager", player.getName());
            PlayerInventory inventory = player.getInventory();
            if (room.getPlayer(player).getIdentity() == PlayerIdentity.PREY) {
                Server.getInstance().getScheduler().scheduleDelayedTask(this.huntGame, () -> {
                    if (room.getStatus() == RoomStatus.GAME && room.isPlaying(player)) {
                        Item item = inventory.getItem(2);
                        int count = item.getCount() + 1;
                        if (item.getId() == 0) {
                            count = 1;
                        }
                        inventory.setItem(2, Item.get(262, 0, count));
                    }
                }, 100);
            }else {
                inventory.addItem(Item.get(262, 0, 1));
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(entity.getLevel().getName());
        if (room == null || room.getStatus() != RoomStatus.GAME) {
            return;
        }
        if (entity.getNetworkId() == 80) {
            Vector3 motion = entity.getMotion();
            entity.setPosition(entity.add(motion.x, motion.y, motion.z));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || event.getItem() == null) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            player.setAllowModifyWorld(false);
        }

        //游戏中禁止与一些方块交互 例如打开箱子
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getBlock();
            switch (block.getId()) {
                case Item.CRAFTING_TABLE:
                case Item.CHEST:
                case Item.ENDER_CHEST:
                case Item.ANVIL:
                case Item.SHULKER_BOX:
                case Item.UNDYED_SHULKER_BOX:
                case Item.FURNACE:
                case Item.BURNING_FURNACE:
                case Item.DISPENSER:
                case Item.DROPPER:
                case Item.HOPPER:
                case Item.BREWING_STAND:
                case Item.CAULDRON:
                case Item.BEACON:
                case Item.FLOWER_POT:
                case Item.JUKEBOX:
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }

        CompoundTag tag = event.getItem().getNamedTag();
        if (tag != null && room.getStatus() == RoomStatus.GAME) {
            if (tag.getBoolean("isHuntGameItem")) {
                event.setCancelled(true);
                Item item = Tools.getHuntGameItem(20, player);
                switch (tag.getInt("HuntGameType")) {
                    case 21:
                        player.getLevel().addParticleEffect(player, ParticleEffect.WATER_EVAPORATION_BUCKET);
                        player.getLevel().addSound(player, Sound.RANDOM_ORB);
                        item.setCount(8);
                        player.getInventory().setItem(4, item);

                        room.getPlayer(player).addEventCount(EventType.PREY_TAUNT_SAFE);
                        break;
                    case 22:
                        player.getLevel().addParticle(new LavaParticle(player));
                        player.getLevel().addParticle(new LavaParticle(player.add(0, 1.5, 0)));
                        player.getLevel().addParticleEffect(player, ParticleEffect.WATER_EVAPORATION_BUCKET);
                        player.getLevel().addSound(player, Sound.RANDOM_LEVELUP);
                        item.setCount(16);
                        player.getInventory().setItem(5, item);

                        room.getPlayer(player).addEventCount(EventType.PREY_TAUNT_DANGER);
                        break;
                    case 23:
                        Tools.spawnFirework(player);
                        item.setCount(32);
                        player.getInventory().setItem(6, item);

                        room.getPlayer(player).addEventCount(EventType.PREY_TAUNT_FIREWORKS);
                        break;
                    case 24:
                        EntityLightning lightning = new EntityLightning(player.chunk, Entity.getDefaultNBT(player));
                        lightning.setEffect(false);
                        lightning.spawnToAll();
                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 10));
                        item.setCount(32);
                        player.getInventory().setItem(7, item);

                        room.getPlayer(player).addEventCount(EventType.PREY_TAUNT_LIGHTNING);
                        break;
                    case 31:
                        ArrayList<Player> list = new ArrayList<>();
                        for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
                            if (entry.getValue().getIdentity() == PlayerIdentity.PREY) {
                                list.add(entry.getKey());
                            }
                        }
                        if (!list.isEmpty()) {
                            list.sort((mapping1, mapping2) -> Double.compare(player.distance(mapping1) - player.distance(mapping2), 0.0D));
                            Player target = list.get(0);
                            FindPlayerEntity projectile = new FindPlayerEntity(player, target);
                            projectile.spawnToAll();

                            Item item1 = Tools.getHuntGameItem(30, player);
                            item1.setCount(64);
                            player.getInventory().setItem(5, item1);

                            target.sendTitle("", this.huntGame.getLanguage(target).translateString("subtitle-lockedByTracker"), 5, 15, 5);
                        }
                        break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null) {
            return;
        }

        Item item = event.getItem();
        if (item.hasCompoundTag() && item.getNamedTag().getBoolean("isHuntGameItem")) {
            if (item.getNamedTag().getInt("HuntGameType") == 10) {
                int nowTick = Server.getInstance().getTick();
                int lastTick = item.getNamedTag().getInt("lastTick");
                if (lastTick == 0 || nowTick - lastTick > 40) {
                    player.sendTitle("", this.huntGame.getLanguage(player).translateString("subtitle_clickAgainToQuitRoom"), 5, 25, 10);
                    CompoundTag tag = item.getNamedTag();
                    tag.putInt("lastTick", nowTick);
                    item.setNamedTag(tag); //防止tag更改不生效，无法退出房间
                    player.getInventory().setItem(8, item);
                    player.getInventory().setHeldItemIndex(7);
                    event.setCancelled(true);
                }else {
                    room.quitRoom(player);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || room.getStatus() != RoomStatus.GAME || !room.isPlaying(player)) {
            return;
        }
        event.setCancelled(true);

        //猎人不能卸下盔甲
        /*if (event.getSlot() >= event.getInventory().getSize() ||
                (room.getPlayers(player) == 1 && event.getSlot() == 8)) {
            event.setCancelled(true);
        }*/
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (HuntGame.debug) {
            return; //debug模式不拦截命令
        }
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getMessage().toLowerCase().startsWith(this.huntGame.getCmdUser(), 1) ||
                event.getMessage().toLowerCase().startsWith(this.huntGame.getCmdAdmin(), 1)) {
            return;
        }
        for (String string : this.huntGame.getCmdWhitelist()) {
            if (string.equalsIgnoreCase(event.getMessage().trim()) || event.getMessage().toLowerCase().startsWith(string, 1)) {
                return;
            }
        }
        event.setMessage("");
        event.setCancelled(true);
        player.sendMessage(this.huntGame.getLanguage(player).translateString("useCmdInRoom"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player) || room.getStatus() != RoomStatus.GAME) {
            return;
        }
        String message = "§7[§a" + Tools.getShowIdentity(room, player) + "§7]§r " + player.getName() + " §b>>>§r " + event.getMessage();
        event.setMessage("");
        event.setCancelled(true);
        for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
            if (entry.getValue().getIdentity() == room.getPlayer(player).getIdentity() ||
                    (room.getPlayer(player).getIdentity() == PlayerIdentity.HUNTER && entry.getValue().getIdentity() == PlayerIdentity.CHANGE_HUNTER) ||
                    (room.getPlayer(player).getIdentity() == PlayerIdentity.CHANGE_HUNTER && entry.getValue().getIdentity() == PlayerIdentity.HUNTER)) {
                entry.getKey().sendMessage(message);
            }
        }
    }

}
