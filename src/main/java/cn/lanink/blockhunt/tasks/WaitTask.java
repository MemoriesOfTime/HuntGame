package cn.lanink.blockhunt.tasks;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;

public class WaitTask extends PluginTask<BlockHunt> {

    private final RoomBase room;

    public WaitTask(BlockHunt owner, RoomBase room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 1) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() >= 5) {
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
                for (Player player : this.room.getPlayers().keySet()) {
                    player.sendActionBar(owner.getLanguage(player).waitTimeBottom
                            .replace("%playerNumber%", room.getPlayers().size() + "")
                            .replace("%time%", room.waitTime + ""));
                    LinkedList<String> ms = new LinkedList<>();
                    for (String string : owner.getLanguage(player).waitTimeScoreBoard.split("\n")) {
                        ms.add(string.replace("%playerNumber%", room.getPlayers().size() + "")
                                .replace("%time%", room.waitTime + ""));
                    }
                    owner.getScoreboard().showScoreboard(player,owner.getLanguage(player).scoreBoardTitle, ms);
                }
            }else {
                this.room.gameStart();
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.getSetWaitTime()) {
                this.room.waitTime = this.room.getSetWaitTime();
            }
            for (Player player : this.room.getPlayers().keySet()) {
                player.sendActionBar(owner.getLanguage(player).waitBottom
                        .replace("%playerNumber%", room.getPlayers().size() + ""));
                LinkedList<String> ms = new LinkedList<>();
                for (String string : owner.getLanguage(player).waitScoreBoard.split("\n")) {
                    ms.add(string.replace("%playerNumber%", room.getPlayers().size() + ""));
                }
                owner.getScoreboard().showScoreboard(player, owner.getLanguage(player).scoreBoardTitle,  ms);
            }
        }else {
            this.room.endGame();
            this.cancel();
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
