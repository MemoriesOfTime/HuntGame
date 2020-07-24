package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.lanink.blockhunt.room.RoomBase;
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
        RoomBase room = this.blockHunt.getRooms().get(player.getLevel().getName());
        if (room != null) {
            room.endGameEvent();
            sender.sendMessage(this.blockHunt.getLanguage(player).adminStopRoom);
        }else {
            sender.sendMessage(this.blockHunt.getLanguage(player).adminLevelNoRoom);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
