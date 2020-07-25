package cn.lanink.blockhunt.ui;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;

public class GuiListener implements Listener {

    private final BlockHunt blockHunt;

    public GuiListener(BlockHunt blockHunt) {
        this.blockHunt = blockHunt;
    }

    /**
     * 玩家操作ui事件
     * 直接执行现有命令，减小代码重复量，也便于维护
     * @param event 事件
     */
    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null || event.getResponse() == null) {
            return;
        }
        GuiType cache = GuiCreate.UI_CACHE.containsKey(player) ? GuiCreate.UI_CACHE.get(player).get(event.getFormID()) : null;
        if (cache == null) return;
        GuiCreate.UI_CACHE.get(player).remove(event.getFormID());
        Language language = BlockHunt.getInstance().getLanguage(player);
        String uName = this.blockHunt.getCmdUser();
        String aName = this.blockHunt.getCmdAdmin();
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple simple = (FormWindowSimple) event.getWindow();
            switch (cache) {
                case USER_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            Server.getInstance().dispatchCommand(player, uName + " join");
                            break;
                        case 1:
                            Server.getInstance().dispatchCommand(player, uName + " quit");
                            break;
                        case 2:
                            GuiCreate.sendRoomListMenu(player);
                            break;
                    }
                    break;
                case ROOM_LIST_MENU:
                    if (simple.getResponse().getClickedButton().getText().equals(language.buttonReturn)) {
                        GuiCreate.sendUserMenu(player);
                    }else {
                        GuiCreate.sendRoomJoinOkMenu(player,
                                simple.getResponse().getClickedButton().getText().split("\n")[0]);
                    }
                    break;
                case ADMIN_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            Server.getInstance().dispatchCommand(player, aName + " setwaitspawn");
                            break;
                        case 1:
                            Server.getInstance().dispatchCommand(player, aName + " addrandomspawn");
                            break;
                        case 2:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 3:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 4:
                            Server.getInstance().dispatchCommand(player, aName + " reloadroom");
                            break;
                        case 5:
                            Server.getInstance().dispatchCommand(player, aName + " unloadroom");
                            break;
                    }
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom custom = (FormWindowCustom) event.getWindow();
            switch (cache) {
                case ADMIN_TIME_MENU:
                    Server.getInstance().dispatchCommand(player, aName + " setwaittime " + custom.getResponse().getInputResponse(0));
                    Server.getInstance().dispatchCommand(player, aName + " setgametime " + custom.getResponse().getInputResponse(1));
                    break;
                case ADMIN_MODE_MENU:
                    Server.getInstance().dispatchCommand(player, aName + " setgamemode " +
                            custom.getResponse().getDropdownResponse(0).getElementContent());
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal modal = (FormWindowModal) event.getWindow();
            if (cache == GuiType.ROOM_JOIN_OK) {
                if (modal.getResponse().getClickedButtonId() == 0 && !modal.getButton1().equals(language.buttonReturn)) {
                    String[] s = modal.getContent().split("\"");
                    Server.getInstance().dispatchCommand(
                            player, uName + " join " + s[1].replace("§e§l", "").trim());
                }else {
                    GuiCreate.sendRoomListMenu(player);
                }
            }
        }
    }

}