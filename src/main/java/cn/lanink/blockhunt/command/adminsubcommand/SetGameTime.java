package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * 设置游戏时间
 *
 * @author lt_name
 */
public class SetGameTime extends BaseSubCommand {

    public SetGameTime(String name) {
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
                int time = Integer.parseInt(args[1]);
                if (time > 60) {
                    Player player = (Player) sender;
                    Config config = this.blockHunt.getRoomConfig(player.getLevel());
                    config.set("gameTime", time);
                    config.save();
                    sender.sendMessage(this.blockHunt.getLanguage(sender).adminSetGameTime.replace("%time%", args[1]));
                } else {
                    sender.sendMessage(this.blockHunt.getLanguage(sender).adminSetGameTimeShort);
                }
            }else {
                sender.sendMessage(this.blockHunt.getLanguage(sender).adminNotNumber);
            }
        }else {
            sender.sendMessage(this.blockHunt.getLanguage(sender).cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("time", CommandParamType.INT) };
    }

}
