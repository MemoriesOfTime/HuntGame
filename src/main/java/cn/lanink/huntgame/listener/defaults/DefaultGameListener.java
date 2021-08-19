package cn.lanink.huntgame.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.FindPlayerEntity;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
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
            if (room.getPlayers(player) == 1) {
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
        CompoundTag tag = event.getItem().getNamedTag();
        if (tag == null) {
            return;
        }
        if (room.getStatus() == RoomStatus.WAIT) {
            if (tag.getBoolean("isHuntGameItem") && tag.getInt("HuntGameType") == 10) {
                room.quitRoom(player);
                event.setCancelled(true);
            }
        }else if (room.getStatus() == RoomStatus.GAME) {
            if (tag.getBoolean("isHuntGameItem")) {
                event.setCancelled(true);
                Item item = Tools.getHuntGameItem(20, player);
                switch (tag.getInt("HuntGameType")) {
                    case 21:
                        player.getLevel().addParticleEffect(player, ParticleEffect.WATER_EVAPORATION_BUCKET);
                        player.getLevel().addSound(player, Sound.RANDOM_ORB);
                        item.setCount(8);
                        player.getInventory().setItem(4, item);
                        break;
                    case 22:
                        player.getLevel().addParticle(new LavaParticle(player));
                        player.getLevel().addParticle(new LavaParticle(player.add(0, 1.5, 0)));
                        player.getLevel().addParticleEffect(player, ParticleEffect.WATER_EVAPORATION_BUCKET);
                        player.getLevel().addSound(player, Sound.RANDOM_LEVELUP);
                        item.setCount(16);
                        player.getInventory().setItem(5, item);
                        break;
                    case 23:
                        Tools.spawnFirework(player);
                        item.setCount(32);
                        player.getInventory().setItem(6, item);
                        break;
                    case 24:
                        EntityLightning lightning = new EntityLightning(player.chunk, Entity.getDefaultNBT(player));
                        lightning.setEffect(false);
                        lightning.spawnToAll();
                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 10));
                        item.setCount(32);
                        player.getInventory().setItem(7, item);
                        break;
                    case 31:
                        ArrayList<Player> list = new ArrayList<>();
                        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                            if (entry.getValue() == 1) {
                                list.add(entry.getKey());
                            }
                        }
                        if (!list.isEmpty()) {
                            list.sort((mapping1, mapping2) -> Double.compare(player.distance(mapping1) - player.distance(mapping2), 0.0D));
                            Player target = list.get(0);
                            FindPlayerEntity projectile = new FindPlayerEntity(player, target);
                            projectile.spawnToAll();

                            Item item1 = Tools.getHuntGameItem(30, player);
                            item1.setCount(32);
                            player.getInventory().setItem(5, item1);
                        }
                        break;
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
        if (event.getMessage().startsWith(this.huntGame.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.huntGame.getCmdAdmin(), 1)) {
            return;
        }
        for (String string : this.huntGame.getCmdWhitelist()) {
            if (string.equalsIgnoreCase(event.getMessage())) {
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
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (entry.getValue() == room.getPlayers(player) ||
                    (room.getPlayers(player) == 2 && entry.getValue() == 12) ||
                    (room.getPlayers(player) == 12 && entry.getValue() ==2)) {
                entry.getKey().sendMessage(message);
            }
        }
    }

}
