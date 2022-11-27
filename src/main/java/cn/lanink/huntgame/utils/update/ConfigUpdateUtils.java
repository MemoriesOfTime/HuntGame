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
        //update1_X_X_To_1_2_2();
    }

    private static void update1_X_X_To_1_2_2() {
        Config config = new Config(HuntGame.getInstance().getDataFolder() + "/config.yml", Config.YAML);
        if (VersionUtils.compareVersion(config.getString("ConfigVersion", "1.0.0"), "1.2.2") >= 0) {
            return;
        }

        config.set("ConfigVersion", "1.2.2");

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("base", "100");
        map.put("taunt-safe", "1");
        map.put("taunt-danger", "2");
        map.put("taunt-fireworks", "3");
        map.put("taunt-lightning", "5");
        map.put("killPrey", "100");
        config.set("integral", map);

        config.save();
    }

}
