package cn.lanink.blockhunt.tasks;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.RoomStatus;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author lt_name
 */
public class VictoryTask extends PluginTask<BlockHunt> {

    private final BaseRoom room;
    private int victoryTime;
    private final int victory;

    public VictoryTask(BlockHunt owner, BaseRoom room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            this.room.getPlayers().keySet().forEach(player -> entry.getKey().showPlayer(player));
            this.room.getLevel().sendBlocks(this.room.getPlayers().keySet().toArray(new Player[0]),
                    new Vector3[] { entry.getKey().floor() });
            if (victory == 2) {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).titleVictoryHuntersTitle,
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).victoryHuntersBottom);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(owner.getLanguage(entry.getKey()).victoryHuntersScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).scoreBoardTitle, ms);
            }else {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).titleVictoryPreySubtitle,
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).victoryPreyBottom);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(owner.getLanguage(entry.getKey()).victoryPreyScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).scoreBoardTitle, ms);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.VICTORY) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.room.endGame(this.victory);
            this.cancel();
        }else {
            this.victoryTime--;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (entry.getValue() != 0) {
                    if (this.victory == 1 && entry.getValue() == 1 ||
                            this.victory == 2 && (entry.getValue() == 2 || entry.getValue() == 12)) {
                        Tools.spawnFirework(entry.getKey());
                    }
                }
            }
        }
    }

}
