package cn.lanink.huntgame.command.adminsubcommand;

import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * 设置等待出生点
 *
 * @author lt_name
 */
public class SetWaitSpawn extends BaseSubCommand {

    public SetWaitSpawn(String name) {
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
        Player player = (Player) sender;
        Config config = this.huntGame.getRoomConfig(player.getLevel());
        String spawn = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        String world = player.getLevel().getName();
        config.set("world", world);
        config.set("waitSpawn", spawn);
        config.save();
        sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminSetSpawn"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
