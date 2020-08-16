package cn.lanink.blockhunt;

import cn.lanink.blockhunt.command.AdminCommand;
import cn.lanink.blockhunt.command.UserCommand;
import cn.lanink.blockhunt.listener.PlayerGameListener;
import cn.lanink.blockhunt.listener.PlayerJoinAndQuit;
import cn.lanink.blockhunt.listener.RoomLevelProtection;
import cn.lanink.blockhunt.room.RoomBase;
import cn.lanink.blockhunt.room.RoomClassicMode;
import cn.lanink.blockhunt.ui.GuiListener;
import cn.lanink.blockhunt.utils.Language;
import cn.lanink.blockhunt.utils.MetricsLite;
import cn.lanink.lib.scoreboard.IScoreboard;
import cn.lanink.lib.scoreboard.ScoreboardDe;
import cn.lanink.lib.scoreboard.ScoreboardGt;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author lt_name
 */
public class BlockHunt extends PluginBase {

    public static final String VERSION = "?";
    private static BlockHunt BLOCK_HUNT;
    private IScoreboard scoreboard;
    public final LinkedList<Integer> taskList = new LinkedList<>();
    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends RoomBase>> ROOM_CLASS = new LinkedHashMap<>();
    private final LinkedHashMap<String, RoomBase> rooms = new LinkedHashMap<>();
    private Config config;
    private String cmdUser, cmdAdmin;
    private final HashMap<String, Language> languageHashMap = new HashMap<>();
    private HashMap<String, String> languageMappingTable;
    private final HashMap<Player, String> playerLanguageHashMap = new HashMap<>();
    private final Skin defaultSkin = new Skin();
    private MetricsLite metricsLite;
    private List<String> victoryCmd;
    private List<String> defeatCmd;
    private boolean hasTips = false;

    public static BlockHunt getInstance() {
        return BLOCK_HUNT;
    }

    @Override
    public void onLoad() {
        BLOCK_HUNT = this;
        saveDefaultConfig();
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.victoryCmd = this.config.getStringList("victoryCmd");
        this.defeatCmd = this.config.getStringList("defeatCmd");
        this.languageMappingTable = this.config.get("languageMap", new HashMap<>());
        //语言文件
        saveResource("Language/zh_CN.yml", false);
        saveResource("Language/en_US.yml", false);
        saveResource("Language/de_DE.yml", false);
        File[] files = new File(getDataFolder() + "/Language").listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String name = file.getName().split("\\.")[0];
                this.languageHashMap.put(name, new Language(new Config(file, Config.YAML)));
                getLogger().info("§aLanguage: " + name + " loaded !");
            }
        }
        //默认皮肤
        this.defaultSkin.setTrusted(true);
        if ("".equals(this.defaultSkin.getSkinResourcePatch().trim())) {
            this.defaultSkin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
        }
        BufferedImage skinData = null;
        try {
            skinData = ImageIO.read(this.getResource("skin.png"));
        } catch (IOException ignored) { }
        if (skinData != null) {
            this.defaultSkin.setSkinData(skinData);
            this.defaultSkin.setSkinId("default");
        }else {
            getLogger().error(this.getLanguage(null).defaultSkinFailure);
        }
        registerRoom("classic", RoomClassicMode.class);
    }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        getLogger().info("§l§eVersion: " + VERSION);
        //加载计分板
        try {
            Class.forName("de.theamychan.scoreboard.ScoreboardPlugin");
            if (getServer().getPluginManager().getPlugin("ScoreboardPlugin").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            this.scoreboard = new ScoreboardDe();
        } catch (Exception e1) {
            try {
                Class.forName("gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI");
                if (getServer().getPluginManager().getPlugin("ScoreboardAPI").isDisabled()) {
                    throw new Exception("Not Loaded");
                }
                this.scoreboard = new ScoreboardGt();
            } catch (Exception e) {
                getLogger().error(this.getLanguage(null).scoreboardAPINotFound);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            this.hasTips = true;
        } catch (Exception ignored) {

        }
        this.cmdUser = this.config.getString("cmdUser", "blockhunt");
        this.cmdAdmin = this.config.getString("cmdAdmin", "blockhuntadmin");
        getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));
        getServer().getPluginManager().registerEvents(new PlayerGameListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        getServer().getPluginManager().registerEvents(new RoomLevelProtection(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.loadRooms();
        try {
            this.metricsLite = new MetricsLite(this, 8298);
        } catch (Exception ignored) {

        }
        getLogger().info(this.getLanguage(null).pluginEnable);
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public boolean isHasTips() {
        return this.hasTips;
    }

    public List<String> getVictoryCmd() {
        return this.victoryCmd;
    }

    public List<String> getDefeatCmd() {
        return this.defeatCmd;
    }

    /**
     * 注册房间类
     *
     * @param name 名称
     * @param roomClass 房间类
     */
    public static void registerRoom(String name, Class<? extends RoomBase> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    public static LinkedHashMap<String, Class<? extends RoomBase>> getRoomClass() {
        return ROOM_CLASS;
    }

    public String getCmdUser() {
        return this.cmdUser;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public IScoreboard getScoreboard() {
        return this.scoreboard;
    }

    /**
     * 传入玩家将返回玩家所用语言
     * 否则返回设置的默认语言
     *
     * @param object 参数
     * @return 语言类
     */
    public Language getLanguage(Object object) {
        if (object instanceof Player) {
            Player player = (Player) object;
            String language = this.playerLanguageHashMap.getOrDefault(player,
                    this.config.getString("defaultLanguage", "zh_CN"));
            if (!this.languageHashMap.containsKey(language)) {
                language = this.languageMappingTable.getOrDefault(language, "zh_CN");
            }
            return this.languageHashMap.get(language);
        }
        return this.languageHashMap.get(this.config.getString("defaultLanguage", "zh_CN"));
    }

    public HashMap<Player, String> getPlayerLanguageHashMap() {
        return this.playerLanguageHashMap;
    }

    public Skin getDefaultSkin() {
        return this.defaultSkin;
    }

    public LinkedHashMap<String, RoomBase> getRooms() {
        return this.rooms;
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getName());
    }

    private Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        Config config = new Config(getDataFolder() + "/Rooms/" + level + ".yml", 2);
        this.roomConfigs.put(level, config);
        return config;
    }

    /**
     * 加载所有房间
     */
    private void loadRooms() {
        getLogger().info(this.getLanguage(null).startLoadingRoom);
        File[] s = new File(getDataFolder() + "/Rooms").listFiles();
        if (s != null && s.length > 0) {
            for (File file1 : s) {
                String[] fileName = file1.getName().split("\\.");
                if (fileName.length > 0) {
                    Config config = getRoomConfig(fileName[0]);
                    if (config.getInt("waitTime", 0) == 0 ||
                            config.getInt("gameTime", 0) == 0 ||
                            "".equals(config.getString("waitSpawn", "").trim()) ||
                            config.getStringList("randomSpawn").size() == 0 ||
                            config.getStringList("blocks").size() == 0 ||
                            "".equals(config.getString("world", "").trim())) {
                        getLogger().warning(this.getLanguage(null).roomLoadedFailureByConfig.replace("%name%", fileName[0]));
                        continue;
                    }
                    String levelName = config.getString("world");
                    if (getServer().getLevelByName(levelName) == null && !getServer().loadLevel(levelName)) {
                        getLogger().warning(this.getLanguage(null).roomLoadedFailureByLevel.replace("%name%", fileName[0]));
                        continue;
                    }
                    try {
                        String gameMode = config.getString("gameMode", "classic");
                        if (!ROOM_CLASS.containsKey(gameMode)) {
                            gameMode = "classic";
                        }
                        Constructor<? extends RoomBase> constructor = ROOM_CLASS.get(gameMode).getConstructor(Config.class);
                        RoomBase roomBase = constructor.newInstance(config);
                        roomBase.setGameName(gameMode);
                        this.rooms.put(fileName[0], roomBase);
                        getLogger().info(this.getLanguage(null).roomLoadedSuccess.replace("%name%", fileName[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        getLogger().info(this.getLanguage(null).roomLoadedAllSuccess.replace(" %number%", this.rooms.size() + ""));
    }

    /**
     * 卸载所有房间
     */
    public void unloadRooms() {
        if (this.rooms.values().size() > 0) {
            Iterator<Map.Entry<String, RoomBase>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, RoomBase> entry = it.next();
                entry.getValue().endGameEvent();
                getLogger().info(this.getLanguage(null).roomUnloadSuccess.replace("%name%", entry.getKey()));
                it.remove();
            }
            this.rooms.clear();
        }
        this.roomConfigs.clear();
        for (int id : this.taskList) {
            getServer().getScheduler().cancelTask(id);
        }
        this.taskList.clear();
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadRooms();
        this.loadRooms();
    }

}
