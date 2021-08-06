package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * 开启房间游戏 跳过等待
 *
 * @author lt_name
 */
public class StartRoom extends BaseSubCommand {

    public StartRoom(String name) {
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
        BaseRoom room = this.blockHunt.getRooms().get(player.getLevel().getName());
        if (room != null) {
            if (room.getPlayers().size() >= 2) {
                if (room.getStatus() == 1) {
                    room.gameStartEvent();
                    sender.sendMessage(this.blockHunt.getLanguage(sender).adminStartRoom);
                }else {
                    sender.sendMessage(this.blockHunt.getLanguage(sender).adminStartRoomIsPlaying);
                }
            }else {
                sender.sendMessage(this.blockHunt.getLanguage(sender).adminStartRoomNoPlayer);
            }
        }else {
            sender.sendMessage(this.blockHunt.getLanguage(sender).adminLevelNoRoom);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
