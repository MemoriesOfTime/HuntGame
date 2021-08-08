package cn.lanink.blockhunt;

import cn.lanink.blockhunt.command.AdminCommand;
import cn.lanink.blockhunt.command.UserCommand;
import cn.lanink.blockhunt.listener.PlayerJoinAndQuit;
import cn.lanink.blockhunt.listener.RoomLevelProtection;
import cn.lanink.blockhunt.listener.animal.AnimalGameListener;
import cn.lanink.blockhunt.listener.block.BlockGameListener;
import cn.lanink.blockhunt.listener.defaults.DefaultGameListener;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.animal.AnimalModeRoom;
import cn.lanink.blockhunt.room.block.BlockModeRoom;
import cn.lanink.blockhunt.ui.GuiListener;
import cn.lanink.blockhunt.utils.Language;
import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.scoreboard.ScoreboardUtil;
import cn.lanink.gamecore.scoreboard.base.IScoreboard;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author lt_name
 */
public class BlockHunt extends PluginBase {

    public static final String VERSION = "?";
    public static boolean debug = true; //TODO close
    public static final Random RANDOM = new Random();

    private static BlockHunt BLOCK_HUNT;

    private IScoreboard scoreboard;

    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final HashMap<String, Class<? extends BaseGameListener>> LISTENER_CLASS = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, BaseGameListener> blockHuntListeners = new HashMap<>();

    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();

    private Config config;

    private String cmdUser;
    private String cmdAdmin;

    private final HashMap<String, Language> languageHashMap = new HashMap<>();
    private HashMap<String, String> languageMappingTable;
    private final HashMap<Player, String> playerLanguageHashMap = new HashMap<>();

    private List<String> victoryCmd;
    private List<String> defeatCmd;

    private boolean hasTips = false;

    public static BlockHunt getInstance() {
        return BLOCK_HUNT;
    }

    @Override
    public void onLoad() {
        BLOCK_HUNT = this;

        this.saveDefaultConfig();
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.victoryCmd = this.config.getStringList("victoryCmd");
        this.defeatCmd = this.config.getStringList("defeatCmd");
        this.languageMappingTable = this.config.get("languageMap", new HashMap<>());

        //语言文件
        this.saveResource("Language/zh_CN.yml", false);
        this.saveResource("Language/en_US.yml", false);
        this.saveResource("Language/de_DE.yml", false);
        File[] files = new File(getDataFolder() + "/Language").listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String name = file.getName().split("\\.")[0];
                this.languageHashMap.put(name, new Language(new Config(file, Config.YAML)));
                getLogger().info("§aLanguage: " + name + " loaded !");
            }
        }

        registerListeners("DefaultGameListener", DefaultGameListener.class);
        registerListeners("BlockGameListener", BlockGameListener.class);
        registerListeners("AnimalGameListener", AnimalGameListener.class);

        registerRoom("block", BlockModeRoom.class);
        if (BlockHunt.debug) {
            registerRoom("animal", AnimalModeRoom.class);
        }
    }

    @Override
    public void onEnable() {
        this.getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        this.getLogger().info("§l§eVersion: " + VERSION);

        //加载计分板
        this.scoreboard = ScoreboardUtil.getScoreboard();

        //检查Tips
        try {
            Class.forName("tip.Main");
            this.hasTips = true;
        } catch (Exception ignored) {

        }

        this.cmdUser = this.config.getString("cmdUser", "blockhunt");
        this.cmdAdmin = this.config.getString("cmdAdmin", "blockhuntadmin");

        this.getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        this.getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));

        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        this.getServer().getPluginManager().registerEvents(new RoomLevelProtection(), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        this.loadAllListener();

        this.loadRooms();

/*        try {
            new MetricsLite(this, ？？？);
        } catch (Exception ignored) {

        }*/

        this.getLogger().info(this.getLanguage(null).pluginEnable);
    }

    @Override
    public void onDisable() {
        this.unloadRooms();
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
        this.blockHuntListeners.put(baseGameListener.getListenerName(), baseGameListener);
        this.getServer().getPluginManager().registerEvents(baseGameListener, this);
        if (BlockHunt.debug) {
            this.getLogger().info("[debug] registerListener: " + baseGameListener.getListenerName());
        }
    }


    @SuppressWarnings("rawtypes")
    public HashMap<String, BaseGameListener> getBlockHuntListeners() {
        return this.blockHuntListeners;
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
        Config config = new Config(getDataFolder() + "/Rooms/" + level + ".yml", 2);
        this.roomConfigs.put(level, config);
        return config;
    }

    /**
     * 加载所有房间
     */
    private void loadRooms() {
        this.getLogger().info(this.getLanguage(null).startLoadingRoom);
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
                        this.getLogger().warning(this.getLanguage(null).roomLoadedFailureByConfig.replace("%name%", fileName[0]));
                        continue;
                    }
                    String levelName = config.getString("world");
                    if (this.getServer().getLevelByName(levelName) == null && !getServer().loadLevel(levelName)) {
                        this.getLogger().warning(this.getLanguage(null).roomLoadedFailureByLevel.replace("%name%", fileName[0]));
                        continue;
                    }
                    try {
                        String gameMode = config.getString("gameMode", "block");
                        if (!ROOM_CLASS.containsKey(gameMode)) {
                            gameMode = "block";
                        }
                        Constructor<? extends BaseRoom> constructor = ROOM_CLASS.get(gameMode).getConstructor(Config.class);
                        BaseRoom baseRoom = constructor.newInstance(config);
                        baseRoom.setGameName(gameMode);
                        this.rooms.put(fileName[0], baseRoom);
                        this.getLogger().info(this.getLanguage(null).roomLoadedSuccess.replace("%name%", fileName[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.getLogger().info(this.getLanguage(null).roomLoadedAllSuccess.replace(" %number%", this.rooms.size() + ""));
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
                this.getLogger().info(this.getLanguage(null).roomUnloadSuccess.replace("%name%", entry.getKey()));
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

}
