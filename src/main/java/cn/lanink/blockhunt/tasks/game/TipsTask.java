package cn.lanink.blockhunt.tasks.game;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomClassicMode;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;

/**
 * 信息显示
 * @author lt_name
 */
public class TipsTask extends PluginTask<BlockHunt> {

    private final RoomClassicMode room;

    public TipsTask(BlockHunt owner, RoomClassicMode room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
            return;
        }
        if (room.getPlayers().values().size() > 0) {
            String mode;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                entry.getKey().setNameTag("");
                switch (entry.getValue()) {
                    case 1:
                        mode = owner.getLanguage(entry.getKey()).prey;
                        break;
                    case 2:
                        mode = owner.getLanguage(entry.getKey()).hunters;
                        break;
                    default:
                        mode = owner.getLanguage(entry.getKey()).death;
                        break;
                }
                LinkedList<String> ms = new LinkedList<>();
                for (String string : owner.getLanguage(entry.getKey()).gameTimeScoreBoard.split("\n")) {
                    ms.add(string.replace("%mode%", mode)
                            .replace("%playerNumber%", this.room.getSurvivorPlayerNumber() + "")
                            .replace("%time%", room.gameTime + ""));
                }
                owner.getScoreboard().showScoreboard(entry.getKey(), owner.getLanguage(entry.getKey()).scoreBoardTitle, ms);
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
