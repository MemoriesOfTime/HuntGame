package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * 重载房间命令
 *
 * @author lt_name
 */
public class ReloadRoom extends BaseSubCommand {

    public ReloadRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.blockHunt.reLoadRooms();
        sender.sendMessage(this.blockHunt.getLanguage(sender).adminReload);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
