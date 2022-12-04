package cn.lanink.huntgame.tasks;

import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.*;
import cn.lanink.huntgame.utils.FormHelper;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
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
        this.victoryTime = 30;
        this.victory = victory == PlayerIdentity.CHANGE_HUNTER ? PlayerIdentity.HUNTER : victory;
        for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
            this.room.getPlayers().keySet().forEach(player -> entry.getKey().showPlayer(player));
            this.room.getLevel().sendBlocks(this.room.getPlayers().keySet().toArray(new Player[0]),
                    new Vector3[] { entry.getKey().floor() }); //清除假方块

            Language language = HuntGame.getInstance().getLanguage(entry.getKey());
            //GUI显示本轮游戏详情
            if (HuntGame.debug) { ///TODO 开发完成后去除
                StringBuilder content = new StringBuilder();
                PlayerData playerData = room.getPlayer(entry.getKey());
                content.append(Tools.getShowIdentity(victory, entry.getKey())).append("获得了本轮游戏的胜利!\n\n");
                content.append("你在本次游戏一共获得了 ").append(playerData.getAllIntegral()).append(" 分\n");
                content.append("积分明细：\n");
                for (EventType eventType : EventType.values()) {
                    if (!eventType.isHasIntegral()) {
                        continue;
                    }
                    int count = playerData.getEventCount(eventType);
                    if (count > 0) {
                        content.append("  ").append(language.translateString("event-" + eventType.name().toLowerCase()))
                                .append(" : ").append(count * IntegralConfig.getIntegral(eventType)).append(" 分\n");
                    }
                }

                AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(FormHelper.PLUGIN_NAME, content.toString());
                simple.addButton(new ElementButton(
                        language.translateString("buttonOK"),
                        new ElementButtonImageData("path", "textures/ui/confirm")
                ));
                simple.showToPlayer(entry.getKey());
            }

            if (victory == PlayerIdentity.HUNTER) {
                entry.getKey().sendTitle(language.translateString("titleVictoryHuntersTitle"),
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(language.translateString("victoryHuntersBottom"));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        language.translateString("scoreBoardTitle"),
                        Arrays.asList(language.translateString("victoryHuntersScoreBoard").split("\n")));
            }else {
                entry.getKey().sendTitle(language.translateString("titleVictoryPreySubtitle"),
                        "", 10, 30, 10);
                entry.getKey().sendActionBar(language.translateString("victoryPreyBottom"));
                owner.getScoreboard().showScoreboard(entry.getKey(),
                        language.translateString("scoreBoardTitle"),
                        Arrays.asList(language.translateString("victoryPreyScoreBoard").split("\n")));
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
