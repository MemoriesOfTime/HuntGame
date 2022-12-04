package cn.lanink.huntgame.utils.update;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.huntgame.HuntGame;
import cn.nukkit.utils.Config;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig() {
        update1_X_X_To_1_2_2();
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
