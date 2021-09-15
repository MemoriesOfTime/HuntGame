package cn.lanink.huntgame.utils;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.variable.BaseVariableV2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LT_Name
 */
public class RsNpcXVariable extends BaseVariableV2 {

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
        HashMap<String, Integer> map = new HashMap<>();
        int all = 0;
        for (BaseRoom room : HuntGame.getInstance().getRooms().values()) {
            String key = room.getGameMode().toLowerCase();
            int playerCount = room.getPlayers().size();
            map.put(key, map.getOrDefault(key, 0) + playerCount);
            all += playerCount;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            this.addVariable("{HuntGameRoomPlayerCountByMode#" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        this.addVariable("{HuntGameRoomPlayerCountAll}", String.valueOf(all));
    }

}
