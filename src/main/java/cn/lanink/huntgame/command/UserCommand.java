package cn.lanink.huntgame.command;

import cn.lanink.huntgame.command.base.BaseCommand;
import cn.lanink.huntgame.command.usersubcommand.JoinRoom;
import cn.lanink.huntgame.command.usersubcommand.QuitRoom;
import cn.lanink.huntgame.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * 玩家命令
 *
 * @author lt_name
 */
public class UserCommand extends BaseCommand {

    public UserCommand(String name) {
        super(name, "HuntGame 命令");
        this.setPermission("huntgame.command.user");
        this.addSubCommand(new JoinRoom("join"));
        this.addSubCommand(new QuitRoom("quit"));
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.huntGame.getLanguage(sender).userHelp.replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUI(CommandSender sender) {
        GuiCreate.sendUserMenu((Player) sender);
    }

}
