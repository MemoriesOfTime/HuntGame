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
    private final static EnumMap<IntegralType, Integer> INTEGER_ENUM_MAP = new EnumMap<>(IntegralType.class);

    private IntegralConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static void init(Config config) {
        INTEGER_ENUM_MAP.put(IntegralType.CUSTOM, 0);
        Map<String, Object> integralMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : (config.get("integral", new HashMap<String, Object>())).entrySet()) {
            integralMap.put(entry.getKey().toUpperCase(), entry.getValue()); //key全部转换为大写
        }
        for (IntegralType integralType : IntegralType.values()) {
            if (integralType == IntegralType.CUSTOM) {
                continue;
            }
            INTEGER_ENUM_MAP.put(integralType, (Integer) integralMap.getOrDefault(integralType.name(), 0));
        }
        if (HuntGame.debug) {
            HuntGame.getInstance().getLogger().info("积分配置：" + INTEGER_ENUM_MAP);
        }
    }

    public static int getIntegral(@NonNull IntegralType integralType) {
        return INTEGER_ENUM_MAP.getOrDefault(integralType, 0);
    }

    public enum IntegralType {

        /**
         * 自定义 (通用)
         */
        CUSTOM,

        /**
         * 游戏开始时玩家基础积分
         */
        BASE,

        /**
         * 安全嘲讽
         */
        TAUNT_SAFE,

        /**
         * 危险嘲讽
         */
        TAUNT_DANGER,

        /**
         * 烟花嘲讽
         */
        TAUNT_FIREWORKS,

        /**
         * 闪电嘲讽
         */
        TAUNT_LIGHTNING,

        /**
         * 击杀猎物
         */
        KILL_PREY,

    }

}
