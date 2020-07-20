package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageBlock;
import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.LinkedList;

/**
 * @author lt_name
 */
public class PlayerGameListener implements Listener {

    private final BlockHunt blockHunt;

    public PlayerGameListener(BlockHunt blockHunt) {
        this.blockHunt = blockHunt;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            RoomBase room = this.blockHunt.getRooms().getOrDefault(damager.getLevel().getName(), null);
            if (room == null) return;
            event.setCancelled(true);
            Entity entity = event.getEntity();
            Item item = damager.getInventory() != null ? damager.getInventory().getItemInHand() : null;
            if (room.getPlayers(damager) == 2 && item != null && item.getId() == 276 &&
                    entity instanceof EntityCamouflageBlock && entity.namedTag != null) {
                Player player = Server.getInstance().getPlayer(entity.namedTag.getString("playerName"));
                room.playerDamageEvent(damager, player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RoomBase room = this.blockHunt.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null) return;
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            player.setAllowModifyWorld(false);
        }
        if (room.getMode() == 1) {
            CompoundTag tag = event.getItem() != null ? event.getItem().getNamedTag() : null;
            if (tag != null && tag.getBoolean("isBlockHuntItem") && tag.getInt("BlockHuntType") == 10) {
                room.quitRoom(player, true);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        RoomBase room = this.blockHunt.getRooms().getOrDefault(event.getTo().getLevel().getName(), null);
        if (room == null || room.getMode() != 2) return;
        Player player = event.getPlayer();
        if (room.getPlayers(player) == 1) {
            Level level = player.getLevel();
            LinkedList<Player> players = new LinkedList<>();
            for (Player p: level.getPlayers().values()) {
                if (p != player) {
                    p.hidePlayer(player);
                    players.add(p);
                }
            }
            Integer[] integers = room.getPlayerCamouflageBlock(player);
            Block block = Block.get(integers[0], integers[1], event.getTo().floor());
            Vector3 vector3 = event.getTo().floor();
            vector3.x += 0.5;
            vector3.z += 0.5;
            room.getEntityCamouflageBlocks(player).setPosition(vector3);
            level.sendBlocks(players.toArray(new Player[0]), new Vector3[] {
                    event.getFrom().floor(), block });
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        RoomBase room = this.blockHunt.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || room.getMode() != 2 || !room.isPlaying(player)) {
            return;
        }
        if (event.getSlot() >= event.getInventory().getSize() ||
                (room.getPlayers(player) == 1 && event.getSlot() == 8)) {
            event.setCancelled(true);
        }
    }

}
