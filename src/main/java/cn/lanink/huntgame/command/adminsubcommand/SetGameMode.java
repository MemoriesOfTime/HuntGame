package cn.lanink.huntgame.command.adminsubcommand;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class SetGameMode extends BaseSubCommand {

    public SetGameMode(String name) {
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
            if (HuntGame.getRoomClass().containsKey(args[1])) {
                Player player = (Player) sender;
                Config config = this.huntGame.getRoomConfig(player.getLevel());
                config.set("gameMode", args[1]);
                config.save();
                sender.sendMessage(this.huntGame.getLanguage(sender).adminSetGameMode.replace("%mode%", args[1]));
            }else {
                sender.sendMessage(this.huntGame.getLanguage(sender).adminSetGameModeNotFound.replace("%mode%", args[1]));
            }
        }else {
            sender.sendMessage(this.huntGame.getLanguage(sender).cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("mode", CommandParamType.TEXT) };
    }

}
