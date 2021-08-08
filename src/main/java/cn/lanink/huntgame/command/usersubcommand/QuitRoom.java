package cn.lanink.huntgame.command.usersubcommand;

import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.lanink.huntgame.room.BaseRoom;
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
        for (BaseRoom room : this.huntGame.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
                sender.sendMessage(this.huntGame.getLanguage(sender).quitRoom);
                return true;
            }
        }
        sender.sendMessage(this.huntGame.getLanguage(sender).quitRoomNotInRoom);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
