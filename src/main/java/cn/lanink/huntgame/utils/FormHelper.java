package cn.lanink.huntgame.utils;

import cn.lanink.gamecore.form.element.ResponseElementButton;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowCustom;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;

import java.util.LinkedList;
import java.util.Map;

public class FormHelper {

    public static final String PLUGIN_NAME = "§l§7[§1H§2u§3n§4t§5G§6a§am§ce§7]";

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);
        simple.addButton(new ResponseElementButton(
                language.translateString("userMenuButton1"),
                new ElementButtonImageData("path", "textures/ui/switch_start_button")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdUser() + " join"))
        );
        simple.addButton(new ResponseElementButton(
                language.translateString("userMenuButton2"),
                new ElementButtonImageData("path", "textures/ui/switch_select_button")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdUser() + " quit"))
        );
        simple.addButton(new ResponseElementButton(
                language.translateString("userMenuButton3"),
                new ElementButtonImageData("path", "textures/ui/servers")
                ).onClicked(FormHelper::sendRoomListMenu)
        );
        simple.showToPlayer(player);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME,
                language.translateString("adminMenuSetLevel").replace("%name%", player.getLevel().getName()));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton1"),
                new ElementButtonImageData("path", "textures/ui/World")
        ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdAdmin() + " setwaitspawn")));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton2"),
                new ElementButtonImageData("path", "textures/ui/World")
        ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdAdmin() + " addrandomspawn")));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton3"),
                new ElementButtonImageData("path", "textures/ui/timer")
        ).onClicked(FormHelper::sendAdminTimeMenu));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton4"),
                new ElementButtonImageData("path", "textures/ui/dev_glyph_color")
        ).onClicked(FormHelper::sendAdminModeMenu));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton5"),
                new ElementButtonImageData("path", "textures/ui/refresh_light")
        ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdAdmin() + " reloadroom")));
        simple.addButton(new ResponseElementButton(
                language.translateString("adminMenuButton6"),
                new ElementButtonImageData("path", "textures/ui/redX1")
        ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdAdmin() + " unloadroom")));
        simple.showToPlayer(player);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminTimeMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText1"), "", "60"));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText2"), "", "300"));

        custom.onResponded((formResponseCustom, cp) -> {
            Server.getInstance().dispatchCommand(player, HuntGame.getInstance().getCmdAdmin() + " setwaittime " + custom.getResponse().getInputResponse(0));
            Server.getInstance().dispatchCommand(player, HuntGame.getInstance().getCmdAdmin() + " setgametime " + custom.getResponse().getInputResponse(1));
        });

        custom.showToPlayer(player);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        LinkedList<String> list = new LinkedList<>();
        for (String mode : HuntGame.getRoomClass().keySet()) {
            if (!list.contains(mode))
                list.add(mode);
        }
        custom.addElement(new ElementDropdown("\n\n\n" +
                language.translateString("adminMenuSetLevel").replace("%name%", player.getLevel().getName()), list));

        custom.onResponded((formResponseCustom, cp) ->
                Server.getInstance().dispatchCommand(player, HuntGame.getInstance().getCmdAdmin() + " setgamemode " +
                        custom.getResponse().getDropdownResponse(0).getElementContent())
        );

        custom.showToPlayer(player);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME, "");
        for (Map.Entry<String, BaseRoom> entry : HuntGame.getInstance().getRooms().entrySet()) {
            simple.addButton(new ResponseElementButton("§e§l" + entry.getKey() +
                    "\n§r§eMode: " + Tools.getShowRoomGameMode(entry.getValue(), player) +
                    " Player: " + entry.getValue().getPlayers().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button")
            ).onClicked(cp -> FormHelper.sendRoomJoinOkMenu(cp, entry.getKey())));
        }
        simple.addButton(new ResponseElementButton(
                language.translateString("buttonReturn"),
                new ElementButtonImageData("path", "textures/ui/cancel")
        ).onClicked(FormHelper::sendUserMenu));
        simple.showToPlayer(player);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     */
    public static void sendRoomJoinOkMenu(Player player, String roomName) {
        Language language = HuntGame.getInstance().getLanguage(player);
        AdvancedFormWindowModal modal;
        BaseRoom room = HuntGame.getInstance().getRooms().get(roomName);
        if (room != null) {
            if (room.getStatus() == RoomStatus.GAME || room.getStatus() == RoomStatus.VICTORY) {
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsPlaying"),
                        language.translateString("buttonReturn"),
                        language.translateString("buttonReturn"));
                modal.onClickedTrue(FormHelper::sendRoomListMenu);
                modal.onClickedFalse(FormHelper::sendRoomListMenu);
            }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsFull"),
                        language.translateString("buttonReturn"),
                        language.translateString("buttonReturn"));
                modal.onClickedTrue(FormHelper::sendRoomListMenu);
                modal.onClickedFalse(FormHelper::sendRoomListMenu);
            }else {
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME,
                        language.translateString("joinRoomOK").replace("%name%", "§e§l\"" + roomName + "\""),
                        language.translateString("buttonOK"),
                        language.translateString("buttonReturn"));
                modal.onClickedTrue(cp -> Server.getInstance().dispatchCommand(cp, HuntGame.getInstance().getCmdUser() + " join " + roomName));
                modal.onClickedFalse(FormHelper::sendRoomListMenu);
            }
        }else {
            modal = new AdvancedFormWindowModal(
                    PLUGIN_NAME,
                    language.translateString("joinRoomIsNotFound"),
                    language.translateString("buttonReturn"),
                    language.translateString("buttonReturn"));
            modal.onClickedTrue(FormHelper::sendRoomListMenu);
            modal.onClickedFalse(FormHelper::sendRoomListMenu);
        }
        modal.showToPlayer(player);
    }

}
