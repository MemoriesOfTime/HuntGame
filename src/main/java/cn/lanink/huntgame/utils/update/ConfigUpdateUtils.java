package cn.lanink.huntgame.utils.update;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.huntgame.HuntGame;
import cn.nukkit.utils.Config;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig() {
        update1_X_X_To_1_2_2();
        update1_2_2_To_1_3_0();
    }

    private static void update1_2_2_To_1_3_0() {
        Config config = new Config(HuntGame.getInstance().getDataFolder() + "/config.yml", Config.YAML);
        if (VersionUtils.compareVersion(config.getString("ConfigVersion", "1.0.0"), "1.3.0") >= 0) {
            return;
        }

        config.set("ConfigVersion", "1.3.0");

        if (!config.exists("victoryRewardCmd")) {
            HashMap<String, List<String>> map = new HashMap<>();
            map.put("0", Collections.singletonList("tell \"@p\" 参与奖！&con"));
            map.put("60", Collections.singletonList("tell \"@p\" 表现良好！&con"));
            map.put("121", Arrays.asList("tell \"@p\" 表现出色！&con", "me I am the best"));

            config.set("victoryRewardCmd", map);
        }

        config.save();
    }

    private static void update1_X_X_To_1_2_2() {
        Config config = new Config(HuntGame.getInstance().getDataFolder() + "/config.yml", Config.YAML);
        if (VersionUtils.compareVersion(config.getString("ConfigVersion", "1.0.0"), "1.2.2") >= 0) {
            return;
        }

        config.set("ConfigVersion", "1.2.2");

        LinkedHashMap<String, Integer> map = config.get("integral", new LinkedHashMap<>());
        if (!map.containsKey("complete_game")) {
            map.put("complete_game", 100);
        }
        if (!map.containsKey("prey_taunt_safe")) {
            map.put("prey_taunt_safe", 1);
        }
        if (!map.containsKey("prey_taunt_danger")) {
            map.put("prey_taunt_danger", 2);
        }
        if (!map.containsKey("prey_taunt_fireworks")) {
            map.put("prey_taunt_fireworks", 3);
        }
        if (!map.containsKey("prey_taunt_lightning")) {
            map.put("prey_taunt_lightning", 5);
        }
        if (!map.containsKey("hunter_kill_prey")) {
            map.put("hunter_kill_prey", 100);
        }
        if (!map.containsKey("prey_bow_hit_hunter")) {
            map.put("prey_bow_hit_hunter", 10);
        }
        config.set("integral", map);

        config.save();
    }

}
