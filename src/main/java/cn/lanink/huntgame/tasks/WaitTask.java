package cn.lanink.huntgame.tasks;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;

public class WaitTask extends PluginTask<HuntGame> {

    private final BaseRoom room;

    public WaitTask(HuntGame owner, BaseRoom room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.WAIT) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() >= room.getMinPlayers()) {
            if (this.room.getPlayers().size() == room.getMaxPlayers() && this.room.waitTime > 10) {
                this.room.waitTime = 10;
            }
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
                for (Player player : this.room.getPlayers().keySet()) {
                    player.sendActionBar(owner.getLanguage(player).translateString("waitTimeBottom")
                            .replace("%playerNumber%", room.getPlayers().size() + "")
                            .replace("%time%", room.waitTime + ""));
                    LinkedList<String> ms = new LinkedList<>();
                    for (String string : owner.getLanguage(player).translateString("waitTimeScoreBoard").split("\n")) {
                        ms.add(string.replace("%gamemode%", Tools.getShowRoomGameMode(this.room, player))
                                .replace("%playerNumber%", this.room.getPlayers().size() + "")
                                .replace("%maxPlayers%", room.getMaxPlayers() + "")
                                .replace("%time%", this.room.waitTime + ""));
                    }
                    owner.getScoreboard().showScoreboard(player,owner.getLanguage(player).translateString("scoreBoardTitle"), ms);
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
                player.sendActionBar(owner.getLanguage(player).translateString("waitBottom")
                        .replace("%playerNumber%", room.getPlayers().size() + ""));
                LinkedList<String> ms = new LinkedList<>();
                for (String string : owner.getLanguage(player).translateString("waitScoreBoard").split("\n")) {
                    ms.add(string.replace("%gamemode%", Tools.getShowRoomGameMode(this.room, player))
                            .replace("%playerNumber%", this.room.getPlayers().size() + "")
                            .replace("%maxPlayers%", room.getMaxPlayers() + "")
                            .replace("%minPlayers%", this.room.getMinPlayers() + ""));
                }
                owner.getScoreboard().showScoreboard(player, owner.getLanguage(player).translateString("scoreBoardTitle"),  ms);
            }
        }else {
            this.room.endGame();
            this.cancel();
        }
    }

}
