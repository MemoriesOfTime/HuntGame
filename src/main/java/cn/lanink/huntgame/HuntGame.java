package cn.lanink.huntgame;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.scoreboard.ScoreboardUtil;
import cn.lanink.gamecore.scoreboard.base.IScoreboard;
import cn.lanink.gamecore.utils.ConfigUtils;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.huntgame.command.AdminCommand;
import cn.lanink.huntgame.command.UserCommand;
import cn.lanink.huntgame.listener.PlayerJoinAndQuit;
import cn.lanink.huntgame.listener.RoomLevelProtection;
import cn.lanink.huntgame.listener.animal.AnimalGameListener;
import cn.lanink.huntgame.listener.block.BlockGameListener;
import cn.lanink.huntgame.listener.defaults.DefaultGameListener;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.IntegralConfig;
import cn.lanink.huntgame.room.animal.AnimalModeRoom;
import cn.lanink.huntgame.room.block.BlockModeRoom;
import cn.lanink.huntgame.utils.MetricsLite;
import cn.lanink.huntgame.utils.RsNpcXVariable;
import cn.lanink.huntgame.utils.update.ConfigUpdateUtils;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author lt_name
 */
public class HuntGame extends PluginBase {

    public static boolean debug = false;

    private static HuntGame huntGame;

    private IScoreboard scoreboard;

    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final HashMap<String, Class<? extends BaseGameListener>> LISTENER_CLASS = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, BaseGameListener> huntGameListeners = new HashMap<>();

    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();

    private Config config;

    private String cmdUser;
    private String cmdAdmin;
    @Getter
    private List<String> cmdWhitelist;

    private final HashMap<String, Language> languageHashMap = new HashMap<>();
    private HashMap<String, String> languageMappingTable;
    private final HashMap<Player, String> playerLanguageHashMap = new HashMap<>();

    @Getter
    private boolean automaticNextRound = false; //游戏结束后自动加入新房间

    private List<String> victoryCmd;
    private List<String> defeatCmd;

    private boolean hasTips = false;

    public static HuntGame getInstance() {
        return huntGame;
    }

    @Override
    public void onLoad() {
        huntGame = this;

        this.saveDefaultConfig();
        ConfigUpdateUtils.updateConfig();
        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        Config configDescription = new Config();
        configDescription.load(this.getResource("Language/ConfigDescription/" + this.config.getString("language", "zh_CN") + ".yml"));
        ConfigUtils.addDescription(this.config, configDescription);

        if (config.getBoolean("debug", false)) {
            debug = true;
            this.getLogger().warning("§c=========================================");
            this.getLogger().warning("§c 警告：您开启了debug模式！");
            this.getLogger().warning("§c Warning: You have turned on debug mode!");
            this.getLogger().warning("§c=========================================");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {

            }
        }

        this.languageMappingTable = this.config.get("languageMap", new HashMap<>());
        //语言文件
        List<String> languages = Arrays.asList("zh_CN", "en_US", "de_DE");
        for (String l : languages) {
            this.saveResource("Language/" + l + ".yml", false);
        }
        File[] files = new File(this.getDataFolder() + "/Language").listFiles();
        if (files != null) {
            for (File file : files) {
                if(file.isFile()) {
                    String name = file.getName().split("\\.")[0];
                    Language language = new Language(new Config(file, Config.YAML));
                    this.languageHashMap.put(name, language);
                    if (languages.contains(name)) {
                        this.saveResource("Language/" + name + ".yml", "Language/cache/new.yml", true);
                        language.update(new Config(this.getDataFolder() + "/Language/cache/new.yml", Config.YAML));
                    }
                    this.getLogger().info("§aLanguage: " + name + " loaded !");
                }
            }
        }

        this.automaticNextRound = this.config.getBoolean("AutomaticNextRound", false);

        this.victoryCmd = this.config.getStringList("victoryCmd");
        this.defeatCmd = this.config.getStringList("defeatCmd");

        registerListeners("DefaultGameListener", DefaultGameListener.class);
        registerListeners("BlockGameListener", BlockGameListener.class);
        registerListeners("AnimalGameListener", AnimalGameListener.class);

        registerRoom("block", BlockModeRoom.class);
        registerRoom("animal", AnimalModeRoom.class);
    }

    @Override
    public void onEnable() {
        this.getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        this.getLogger().info("§l§eVersion: " + this.getVersion());

        //检查依赖版本
        try {
            String needGameCoreVersion = "1.6.5";
            if (!VersionUtils.checkMinimumVersion(GameCore.getInstance(), needGameCoreVersion)) {
                throw new RuntimeException("[MemoriesOfTime-GameCore] plugin version is too low! At least version " + needGameCoreVersion + " is needed!");
            }
        }catch (Exception e) {
            this.getLogger().error("Please check the dependency plugin version!", e);
            this.getLogger().error("Please update the plugin to the required version!");
            //延迟3秒方便查看报错
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {

            }
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //加载计分板
        this.scoreboard = ScoreboardUtil.getScoreboard();

        //检查Tips
        try {
            Class.forName("tip.Main");
            this.hasTips = true;
        } catch (Exception ignored) {

        }

        this.cmdUser = this.config.getString("cmdUser", "HuntGame").toLowerCase();
        this.cmdAdmin = this.config.getString("cmdAdmin", "HuntGameAdmin").toLowerCase();
        this.cmdWhitelist = new ArrayList<>();
        for (String cmd : this.config.getStringList("cmdWhitelist")) {
            this.cmdWhitelist.add(cmd.toLowerCase());
        }

        IntegralConfig.init(this.config);

        this.getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        this.getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));

        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        this.getServer().getPluginManager().registerEvents(new RoomLevelProtection(), this);

        this.loadAllListener();

        this.loadRooms();

        try {
            Class.forName("com.smallaswater.npc.variable.BaseVariableV2");
            VariableManage.addVariableV2("HuntGame", RsNpcXVariable.class);
        }catch (Exception ignored) {

        }

        try {
            new MetricsLite(this, 12405);
        } catch (Exception ignored) {

        }

        this.getLogger().info(this.getLanguage().translateString("pluginEnable"));
    }

    @Override
    public void onDisable() {
        this.unloadRooms();
        this.getLogger().info(this.getLanguage().translateString("pluginDisable"));
    }

    /**
     * 注册监听器类
     *
     * @param name 名称
     * @param listenerClass 监听器类
     */
    @SuppressWarnings("rawtypes")
    public static void registerListeners(String name, Class<? extends BaseGameListener> listenerClass) {
        LISTENER_CLASS.put(name, listenerClass);
    }

    @SuppressWarnings("rawtypes")
    public void loadAllListener() {
        for (Map.Entry<String, Class<? extends BaseGameListener>> entry : LISTENER_CLASS.entrySet()) {
            try {
                BaseGameListener listener = entry.getValue().newInstance();
                listener.init(entry.getKey());
                this.loadListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void loadListener(BaseGameListener baseGameListener) {
        this.huntGameListeners.put(baseGameListener.getListenerName(), baseGameListener);
        this.getServer().getPluginManager().registerEvents(baseGameListener, this);
        if (HuntGame.debug) {
            this.getLogger().info("[debug] registerListener: " + baseGameListener.getListenerName());
        }
    }


    @SuppressWarnings("rawtypes")
    public HashMap<String, BaseGameListener> getHuntGameListeners() {
        return this.huntGameListeners;
    }

    public static boolean hasRoomClass(String name) {
        return ROOM_CLASS.containsKey(name);
    }

    /**
     * 注册房间类
     *
     * @param name 名称
     * @param roomClass 房间类
     */
    public static void registerRoom(String name, Class<? extends BaseRoom> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getRoomClass() {
        return ROOM_CLASS;
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

    public String getCmdUser() {
        return this.cmdUser;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public IScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public Language getLanguage() {
        return this.getLanguage(null);
    }

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
        return GameCore.DEFAULT_SKIN;
    }

    public LinkedHashMap<String, BaseRoom> getRooms() {
        return this.rooms;
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getName());
    }

    private Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        Config config = new Config(this.getDataFolder() + "/Rooms/" + level + ".yml", Config.YAML);
        this.roomConfigs.put(level, config);
        return config;
    }

    /**
     * 加载所有房间
     */
    private void loadRooms() {
        this.getLogger().info(this.getLanguage().translateString("startLoadingRoom"));
        File[] s = new File(this.getDataFolder() + "/Rooms").listFiles();
        if (s != null) {
            for (File file : s) {
                String[] fileName = file.getName().split("\\.");
                if (fileName.length > 0) {
                    String levelName = fileName[0];
                    Config config = getRoomConfig(levelName);
                    if (config.getInt("waitTime", 0) == 0 ||
                            config.getInt("gameTime", 0) == 0 ||
                            "".equals(config.getString("waitSpawn", "").trim()) ||
                            config.getStringList("randomSpawn").size() == 0) {
                        this.getLogger().warning(this.getLanguage().translateString("roomLoadedFailureByConfig").replace("%name%", fileName[0]));
                        continue;
                    }
                    if (this.getServer().getLevelByName(levelName) == null && !getServer().loadLevel(levelName)) {
                        this.getLogger().warning(this.getLanguage().translateString("roomLoadedFailureByLevel").replace("%name%", fileName[0]));
                        continue;
                    }
                    try {
                        String gameMode = config.getString("gameMode").toLowerCase();
                        if (!ROOM_CLASS.containsKey(gameMode)) {
                            gameMode = "block";
                        }
                        Constructor<? extends BaseRoom> constructor = ROOM_CLASS.get(gameMode).getConstructor(Level.class, Config.class);
                        BaseRoom baseRoom = constructor.newInstance(this.getServer().getLevelByName(levelName), config);
                        baseRoom.setGameName(gameMode);
                        this.rooms.put(fileName[0], baseRoom);
                        this.getLogger().info(this.getLanguage().translateString("roomLoadedSuccess").replace("%name%", fileName[0]));
                    } catch (Exception e) {
                        this.getLogger().error("加载房间时出错：", e);
                    }
                }
            }
        }
        this.getLogger().info(this.getLanguage().translateString("roomLoadedAllSuccess").replace(" %number%", this.rooms.size() + ""));
    }

    /**
     * 卸载所有房间
     */
    public void unloadRooms() {
        if (this.rooms.size() > 0) {
            Iterator<Map.Entry<String, BaseRoom>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, BaseRoom> entry = it.next();
                entry.getValue().endGame();
                this.getLogger().info(this.getLanguage().translateString("roomUnloadSuccess").replace("%name%", entry.getKey()));
                it.remove();
            }
            this.rooms.clear();
        }
        this.roomConfigs.clear();
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadRooms();
        this.loadRooms();
    }

    public String getVersion() {
        Config config = new Config(Config.PROPERTIES);
        config.load(this.getResource("git.properties"));
        return config.get("git.build.version", this.getDescription().getVersion()) + " git-" + config.get("git.commit.id.abbrev", "Unknown");
    }

}
