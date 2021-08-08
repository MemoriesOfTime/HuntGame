package cn.lanink.huntgame.tasks;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.Map;

/**
 * @author lt_name
 */
public class VictoryTask extends PluginTask<HuntGame> {

    private final BaseRoom room;
    private int victoryTime;
    private final int victory;

    public VictoryTask(HuntGame owner, BaseRoom room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            this.room.getPlayers().keySet().forEach(player -> entry.getKey().showPlayer(player));
            this.room.getLevel().sendBlocks(this.room.getPlayers().keySet().toArray(new Player[0]),
                    new Vector3[] { entry.getKey().floor() });
            if (victory == 2) {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).translateString("titleVictoryHuntersTitle"),
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).translateString("victoryHuntersBottom"));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).translateString("scoreBoardTitle"),
                        Arrays.asList(owner.getLanguage(entry.getKey()).translateString("victoryHuntersScoreBoard").split("\n")));
            }else {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).translateString("titleVictoryPreySubtitle"),
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).translateString("victoryPreyBottom"));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).translateString("scoreBoardTitle"),
                        Arrays.asList(owner.getLanguage(entry.getKey()).translateString("victoryPreyScoreBoard").split("\n")));
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
