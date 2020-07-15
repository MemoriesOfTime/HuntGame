package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author lt_name
 */
public class PlayerGameListener implements Listener {

    private final BlockHunt blockHunt;

    public PlayerGameListener(BlockHunt blockHunt) {
        this.blockHunt = blockHunt;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RoomBase room = this.blockHunt.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || room.getMode() != 2) return;
        if (room.getPlayers(player) == 2) {
            Vector3 vector3 = event.getBlock().getLocation();
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (entry.getValue() == 1) {
                    if (vector3.getFloorX() == entry.getKey().getFloorX() &&
                            vector3.getFloorY() == entry.getKey().getFloorY() &&
                            vector3.getFloorZ() == entry.getKey().getFloorZ()) {
                        room.playerDamage(player, entry.getKey());
                    }
                }
            }

        }
        event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        RoomBase room = this.blockHunt.getRooms().getOrDefault(event.getTo().getLevel().getName(), null);
        if (room == null || room.getMode() != 2) return;
        Player player = event.getPlayer();
        if (room.getPlayers(player) == 1) {
            Level level = player.getLevel();
            LinkedList<Player> players = new LinkedList<>();
            for (Player p: room.getPlayers().keySet()) {
                if (p != player) {
                    p.hidePlayer(player);
                    players.add(p);
                }
            }
            Block block = level.getBlock(event.getFrom());
            block.x = event.getFrom().getFloorX();
            block.y = event.getFrom().getFloorY();
            block.z = event.getFrom().getFloorZ();
            level.sendBlocks(players.toArray(new Player[0]), new Block[] { block });
            Integer[] integers = room.getPlayerCamouflageBlock(player);
            block = Block.get(integers[0], integers[1], event.getTo());
            level.sendBlocks(players.toArray(new Player[0]), new Block[] { block });
        }
    }


}
