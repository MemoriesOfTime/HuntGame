package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * 卸载所有房间
 *
 * @author lt_name
 */
public class UnloadRoom extends BaseSubCommand {

    public UnloadRoom(String name) {
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
        this.blockHunt.unloadRooms();
        sender.sendMessage(this.blockHunt.getLanguage(sender).adminUnload);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
