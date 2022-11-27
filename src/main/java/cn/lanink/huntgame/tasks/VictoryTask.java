package cn.lanink.huntgame.tasks;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.PlayerData;
import cn.lanink.huntgame.room.PlayerIdentity;
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
    private final PlayerIdentity victory;

    public VictoryTask(HuntGame owner, BaseRoom room, PlayerIdentity victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory == PlayerIdentity.CHANGE_HUNTER ? PlayerIdentity.HUNTER : victory;
        for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
            this.room.getPlayers().keySet().forEach(player -> entry.getKey().showPlayer(player));
            this.room.getLevel().sendBlocks(this.room.getPlayers().keySet().toArray(new Player[0]),
                    new Vector3[] { entry.getKey().floor() });
            if (victory == PlayerIdentity.HUNTER) {
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
            for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
                if (this.owner.isAutomaticNextRound()) {
                    entry.getKey().sendTip(this.owner.getLanguage(entry.getKey()).translateString("victory_automaticallyJoinTheNextGameCountdown_Bottom", this.victoryTime));
                }
                PlayerIdentity identity = entry.getValue().getIdentity();
                if (identity != PlayerIdentity.NULL) {
                    if (this.victory == PlayerIdentity.PREY && identity == PlayerIdentity.PREY ||
                            this.victory == PlayerIdentity.HUNTER && (identity == PlayerIdentity.HUNTER || identity == PlayerIdentity.CHANGE_HUNTER)) {
                        Tools.spawnFirework(entry.getKey());
                    }
                }
            }
        }
    }

}
