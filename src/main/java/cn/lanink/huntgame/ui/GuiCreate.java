package cn.lanink.huntgame.ui;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.Task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GuiCreate {

    public static final String PLUGIN_NAME = "§l§7[§1B§2l§3o§4c§5k§6H§au§cn§bt§7]";
    public static HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>(); //ui缓存

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ElementButton(language.translateString("userMenuButton1"),
                new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        simple.addButton(new ElementButton(language.translateString("userMenuButton2"),
                new ElementButtonImageData("path", "textures/ui/switch_select_button")));
        simple.addButton(new ElementButton(language.translateString("userMenuButton3"),
                new ElementButtonImageData("path", "textures/ui/servers")));
        showFormWindow(player, simple, GuiType.USER_MENU);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME,
                language.translateString("adminMenuSetLevel").replace("%name%", player.getLevel().getName()));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton1"),
                new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton2"),
                new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton3"),
                new ElementButtonImageData("path", "textures/ui/timer")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton4"),
                new ElementButtonImageData("path", "textures/ui/dev_glyph_color")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton5"),
                new ElementButtonImageData("path", "textures/ui/refresh_light")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton6"),
                new ElementButtonImageData("path", "textures/ui/redX1")));
        showFormWindow(player, simple, GuiType.ADMIN_MENU);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminTimeMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText1"), "", "60"));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText2"), "", "300"));
        showFormWindow(player, custom, GuiType.ADMIN_TIME_MENU);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        LinkedList<String> list = new LinkedList<>();
        for (String mode : HuntGame.getRoomClass().keySet()) {
            if (!list.contains(mode))
                list.add(mode);
        }
        custom.addElement(new ElementDropdown("\n\n\n" +
                language.translateString("adminMenuSetLevel").replace("%name%", player.getLevel().getName()), list));
        showFormWindow(player, custom, GuiType.ADMIN_MODE_MENU);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        for (Map.Entry<String, BaseRoom> entry : HuntGame.getInstance().getRooms().entrySet()) {
            simple.addButton(new ElementButton("§e§l" + entry.getKey() +
                    "\n§r§eMode: " + entry.getValue().getGameMode() +
                    " Player: " + entry.getValue().getPlayers().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        }
        simple.addButton(new ElementButton(language.translateString("buttonReturn"),
                new ElementButtonImageData("path", "textures/ui/cancel")));
        showFormWindow(player, simple, GuiType.ROOM_LIST_MENU);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     */
    public static void sendRoomJoinOkMenu(Player player, String roomName) {
        Language language = HuntGame.getInstance().getLanguage(player);
        FormWindowModal modal;
        BaseRoom room = HuntGame.getInstance().getRooms().get(roomName.replace("§e§l", "").trim());
        if (room != null) {
            if (room.getStatus() == RoomStatus.GAME || room.getStatus() == RoomStatus.VICTORY) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsPlaying"),
                        language.translateString("buttonReturn"), language.translateString("buttonReturn"));
            }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsFull"),
                        language.translateString("buttonReturn"), language.translateString("buttonReturn"));
            }else {
                modal = new FormWindowModal(
                        PLUGIN_NAME,
                        language.translateString("joinRoomOK").replace("%name%", "\"" + roomName + "\""),
                        language.translateString("buttonOK"), language.translateString("buttonReturn"));
            }
        }else {
            modal = new FormWindowModal(
                    PLUGIN_NAME,
                    language.translateString("joinRoomIsNotFound"),
                    language.translateString("buttonReturn"), language.translateString("buttonReturn"));
        }
        showFormWindow(player, modal, GuiType.ROOM_JOIN_OK);
    }

    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        HashMap<Integer, GuiType> map;
        if (!UI_CACHE.containsKey(player)) {
            map = new HashMap<>();
            UI_CACHE.put(player, map);
        }else {
            map = UI_CACHE.get(player);
        }
        int id = player.showFormWindow(window);
        map.put(id, guiType);
        Server.getInstance().getScheduler().scheduleDelayedTask(HuntGame.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (UI_CACHE.containsKey(player))
                    UI_CACHE.get(player).remove(id);
            }
        }, 2400);
    }

}
