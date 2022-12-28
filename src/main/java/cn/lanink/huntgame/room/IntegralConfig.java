package cn.lanink.huntgame.room;

import cn.lanink.huntgame.HuntGame;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LT_Name
 */
public class IntegralConfig {

    @Getter
    private final static EnumMap<EventType, Integer> INTEGER_ENUM_MAP = new EnumMap<>(EventType.class);

    private IntegralConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static void init(Config config) {
        Map<String, Object> integralMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : (config.get("integral", new HashMap<String, Object>())).entrySet()) {
            integralMap.put(entry.getKey().toUpperCase(), entry.getValue()); //key全部转换为大写
        }
        for (EventType eventType : EventType.values()) {
            if (eventType.isHasIntegral()) {
                INTEGER_ENUM_MAP.put(eventType, (Integer) integralMap.getOrDefault(eventType.name(), 0));
            }
        }
        if (HuntGame.debug) {
            HuntGame.getInstance().getLogger().info("积分配置：" + INTEGER_ENUM_MAP);
        }
    }

    public static int getIntegral(@NonNull EventType eventType) {
        if (!eventType.isHasIntegral()) {
            return 0;
        }
        return INTEGER_ENUM_MAP.getOrDefault(eventType, 0);
    }

}
