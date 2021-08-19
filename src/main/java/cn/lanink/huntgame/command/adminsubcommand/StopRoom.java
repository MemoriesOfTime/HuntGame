package cn.lanink.huntgame.command.adminsubcommand;

import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * @author lt_name
 */
public class StopRoom extends BaseSubCommand {

    public StopRoom(String name) {
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
        BaseRoom room = this.huntGame.getRooms().get(player.getLevel().getName());
        if (room != null) {
            room.endGame();
            sender.sendMessage(this.huntGame.getLanguage(player).translateString("adminStopRoom"));
        }else {
            sender.sendMessage(this.huntGame.getLanguage(player).translateString("adminLevelNoRoom"));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
