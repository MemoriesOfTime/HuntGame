package cn.lanink.huntgame.command.adminsubcommand;

import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * 设置等待时间
 *
 * @author lt_name
 */
public class SetWaitTime extends BaseSubCommand {

    public SetWaitTime(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 2) {
            if (args[1].matches("[0-9]*")) {
                Player player = (Player) sender;
                Config config = this.huntGame.getRoomConfig(player.getLevel());
                config.set("waitTime", Integer.parseInt(args[1]));
                config.save();
                sender.sendMessage(this.huntGame.getLanguage(sender)
                        .translateString("adminSetWaitTime").replace("%time%", args[1]));
            }else {
                sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminNotNumber"));
            }
        }else {
            sender.sendMessage(this.huntGame.getLanguage(sender)
                    .translateString("cmdHelp").replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("time", CommandParamType.INT) };
    }

}
