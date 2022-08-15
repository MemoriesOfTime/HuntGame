package cn.lanink.huntgame.tasks;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.nukkit.scheduler.PluginTask;
import net.fap.stage.FStage;

/**
 * @author LT_Name
 */
public class FStageTask extends PluginTask<HuntGame> {

    public FStageTask(HuntGame huntGame) {
        super(huntGame);
    }

    @Override
    public void onRun(int i) {
        for (BaseRoom room : this.owner.getRooms().values()) {
            if (room.getStatus() == RoomStatus.TASK_NEED_INITIALIZED || room.getStatus() == RoomStatus.WAIT) {
                FStage.setLocalStatus("free");
                return;
            }
        }
        FStage.setLocalStatus("run");
    }

    @Override
    public void onCancel() {
        FStage.setLocalStatus("close");
    }

}
