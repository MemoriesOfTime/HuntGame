package cn.lanink.blockhunt.listener;

import cn.lanink.blockhunt.BlockHunt;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;

import java.util.LinkedList;

/**
 * @author lt_name
 */
public class PlayerGameListener implements Listener {

    public PlayerGameListener(BlockHunt blockHunt) {

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getFrom().equals(event.getTo())) {
            if (event.getPlayer().getName().equals("ltname233")) {

                LinkedList<Player> players = new LinkedList<>();
                event.getTo().getLevel().getPlayers().values().forEach(player -> {
                    if (!player.getName().equals("ltname233")) {
                        players.add(player);
                        player.hidePlayer(event.getPlayer());
                    }
                });

                Block block = event.getFrom().getLevel().getBlock(event.getFrom());
                block.x = event.getFrom().getFloorX();
                block.y = event.getFrom().getFloorY();
                block.z = event.getFrom().getFloorZ();
                event.getFrom().getLevel().sendBlocks(players.toArray(new Player[0]), new Block[] { block });

                block = Block.get(2);
                block.x = event.getTo().getFloorX();
                block.y = event.getTo().getFloorY();
                block.z = event.getTo().getFloorZ();
                event.getTo().getLevel().sendBlocks(players.toArray(new Player[0]), new Block[] { block });
            }
        }
    }


}
