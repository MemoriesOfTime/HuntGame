package cn.lanink.huntgame.command.adminsubcommand;

import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
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
        BaseRoom room = this.huntGame.getRooms().get(player.getLevel().getName());
        if (room != null) {
            if (room.getPlayers().size() >= room.getMinPlayers()) {
                if (room.getStatus() == RoomStatus.WAIT) {
                    room.gameStart();
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminStartRoom"));
                }else {
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminStartRoomIsPlaying"));
                }
            }else {
                sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminStartRoomNoPlayer"));
            }
        }else {
            sender.sendMessage(this.huntGame.getLanguage(sender).translateString("adminLevelNoRoom"));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
