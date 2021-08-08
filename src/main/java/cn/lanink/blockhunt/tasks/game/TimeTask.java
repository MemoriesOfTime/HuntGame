package cn.lanink.blockhunt.tasks.game;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.RoomStatus;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<BlockHunt> {

    private final BaseRoom room;

    public TimeTask(BlockHunt owner, BaseRoom room) {
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
