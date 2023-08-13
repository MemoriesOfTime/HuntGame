package cn.lanink.huntgame.utils.update;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.huntgame.HuntGame;
import cn.nukkit.utils.Config;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig() {
        update1_X_X_To_1_2_2();
        update1_2_2_To_1_3_0();
        update1_3_0_To_1_3_1();
    }

    @NotNull
    private static Config getConfig() {
        return new Config(HuntGame.getInstance().getDataFolder() + "/config.yml", Config.YAML);
    }

    private static void update1_3_0_To_1_3_1() {
        Config config = getConfig();
        if (VersionUtils.compareVersion(config.getString("ConfigVersion", "1.0.0"), "1.3.1") >= 0) {
            return;
        }

        config.set("ConfigVersion", "1.3.1");

        if (!config.exists("AutomaticJoinGame")) {
            config.set("AutomaticJoinGame", false);
        }

        config.save();
    }

    private static void update1_2_2_To_1_3_0() {
        Config config = getConfig();
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
        Config config = getConfig();
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

    // 需要在NsGB加载后检查，放到onEnable里
    public static void checkFapNsGB(HuntGame huntGame) {
        try {
            Class.forName("cn.nsgamebase.NsGameBaseMain");

            Config config = getConfig();

            LinkedHashMap<String, Object> fapRewardIntegral = new LinkedHashMap<>();

            LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
            map1.put("money", 10);
            map1.put("point", 0);
            map1.put("exp", 10);
            map1.put("maxMultiplier", 1);

            LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
            map2.put("money", 100);
            map2.put("point", 0);
            map2.put("exp", 100);
            map2.put("maxMultiplier", 1);

            fapRewardIntegral.put("0", map1);
            fapRewardIntegral.put("60", map2);

            boolean needSave = false;
            if (!config.exists("fapRewardIntegral")) {
                config.set("fapRewardIntegral", fapRewardIntegral);
                needSave = true;
            }

            if (needSave) {
                config.save();
            }
        } catch (Exception ignored) {

        }
    }

}
