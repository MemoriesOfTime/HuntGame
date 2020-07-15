package cn.lanink.blockhunt.tasks;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author lt_name
 */
public class VictoryTask extends PluginTask<BlockHunt> {

    private final RoomBase room;
    private int victoryTime;
    private final int victory;

    public VictoryTask(BlockHunt owner, RoomBase room, int victory) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (victory == 3) {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).titleVictoryKillerTitle,
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).victoryKillerBottom);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(owner.getLanguage(entry.getKey()).victoryKillerScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).scoreBoardTitle, ms);
            }else {
                entry.getKey().sendTitle(owner.getLanguage(entry.getKey()).titleVictoryCommonPeopleSubtitle,
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(owner.getLanguage(entry.getKey()).victoryCommonPeopleBottom);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(owner.getLanguage(entry.getKey()).victoryCommonPeopleScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        owner.getLanguage(entry.getKey()).scoreBoardTitle, ms);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.cancel();
            this.room.endGame();
        }else {
            this.victoryTime--;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (entry.getValue() != 0) {
                    if (this.victory == 1 && entry.getValue() == 3) {
                        continue;
                    }
                    Tools.spawnFirework(entry.getKey());
                }
            }
        }
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
