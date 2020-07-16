package cn.lanink.blockhunt.command.usersubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.lanink.blockhunt.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class QuitRoom extends BaseSubCommand {

    public QuitRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "退出" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        for (RoomBase room : this.blockHunt.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player, true);
                sender.sendMessage(this.blockHunt.getLanguage(sender).quitRoom);
                return true;
            }
        }
        sender.sendMessage(this.blockHunt.getLanguage(sender).quitRoomNotInRoom);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
