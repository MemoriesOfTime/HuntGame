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
        this.victoryTime = 10;
        this.victory = victory == PlayerIdentity.CHANGE_HUNTER ? PlayerIdentity.HUNTER : victory;
        for (Map.Entry<Player, PlayerData> entry : room.getPlayers().entrySet()) {
            this.room.getPlayers().keySet().forEach(player -> entry.getKey().showPlayer(player));
            this.room.getLevel().sendBlocks(this.room.getPlayers().keySet().toArray(new Player[0]),
                    new Vector3[] { entry.getKey().floor() }); //清除假方块

            //TODO 改为GUI
            if (HuntGame.debug) { ///TODO 开发完成后去除
                Language language = HuntGame.getInstance().getLanguage(entry.getKey());
                StringBuilder content = new StringBuilder();
                PlayerData playerData = room.getPlayer(entry.getKey());
                if (victory == PlayerIdentity.HUNTER) {
                    content.append("猎人获得了胜利!\n\n");
                }else {
                    content.append("猎物获得了胜利!\n\n");
                }
                content.append("你在本次游戏一共获得了 ").append(playerData.getAllIntegral()).append(" 分\n");
                content.append("积分明细：\n  基础得分： ").append(playerData.getIntegral(IntegralConfig.IntegralType.BASE)).append(" 分\n");
                content.append("  击杀猎物： ").append(playerData.getIntegral(IntegralConfig.IntegralType.KILL_PREY)).append(" 分\n");
                content.append("  安全嘲讽： ").append(playerData.getIntegral(IntegralConfig.IntegralType.TAUNT_SAFE)).append(" 分\n");
                content.append("  危险嘲讽： ").append(playerData.getIntegral(IntegralConfig.IntegralType.TAUNT_DANGER)).append(" 分\n");
                content.append("  烟花嘲讽： ").append(playerData.getIntegral(IntegralConfig.IntegralType.TAUNT_FIREWORKS)).append(" 分\n");
                content.append("  闪电嘲讽： ").append(playerData.getIntegral(IntegralConfig.IntegralType.TAUNT_LIGHTNING)).append(" 分\n");

                AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(FormHelper.PLUGIN_NAME, content.toString());
                simple.addButton(new ElementButton(
                        language.translateString("buttonOK"),
                        new ElementButtonImageData("path", "textures/ui/confirm")
                ));
                simple.showToPlayer(entry.getKey());
            }

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
