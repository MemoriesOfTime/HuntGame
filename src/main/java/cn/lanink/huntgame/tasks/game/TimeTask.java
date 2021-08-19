package cn.lanink.huntgame.tasks.game;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<HuntGame> {

    private final BaseRoom room;

    public TimeTask(HuntGame owner, BaseRoom room) {
        super(owner);
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.GAME) {
            this.cancel();
            return;
        }
        this.room.timeTask();
    }

}
