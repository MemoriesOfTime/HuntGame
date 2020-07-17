package cn.lanink.blockhunt.command;

import cn.lanink.blockhunt.command.base.BaseCommand;
import cn.lanink.blockhunt.command.usersubcommand.JoinRoom;
import cn.lanink.blockhunt.command.usersubcommand.QuitRoom;
import cn.nukkit.command.CommandSender;

/**
 * 玩家命令
 *
 * @author lt_name
 */
public class UserCommand extends BaseCommand {

    public UserCommand(String name) {
        super(name, "BlockHunt 命令");
        this.setPermission("blockhunt.command.user");
        this.addSubCommand(new JoinRoom("join"));
        this.addSubCommand(new QuitRoom("quit"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.blockHunt.getLanguage(sender).userHelp.replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUI(CommandSender sender) {
        this.sendHelp(sender);
        //TODO GUI!
        //GuiCreate.sendUserMenu((Player) sender);
    }

}
